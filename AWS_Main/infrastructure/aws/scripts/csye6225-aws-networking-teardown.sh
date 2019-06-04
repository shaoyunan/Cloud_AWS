#!/bin/bash
# create-aws-vpc
# variables used in script:
# download jq: sudo apt-get install jq

if [ ! -n "$1" ]
then
aws_response=$(aws ec2 describe-vpcs\
 --filter "Name=isDefault,Values=false")
echo "These are your VPCids"
echo $(echo -e "$aws_response" |  jq '.Vpcs[].VpcId' | tr -d '"')
echo "Next time enter a VPCid to teardown"
sleep 1
exit
fi

function f(){
if [ $? -ne 0 ]
then
echo "Goodby"
sleep 2
exit
fi
}

clear
echo "Deleting VPC..."

vpcId="$1"
cidr="0.0.0.0/0"

routetableId=$(aws ec2 describe-route-tables \
 --filters "Name=vpc-id,Values=$vpcId" \
 "Name=association.main,Values=false"| jq '.RouteTables[].RouteTableId' | tr -d '"')

#deleting subnets
subnetId=$(aws ec2 describe-subnets \
 --filters "Name=vpc-id,Values=$vpcId" | jq '.Subnets[].SubnetId' | tr -d '"')
for id in $subnetId
do
aws ec2 delete-subnet \
--subnet-id $id
f
echo "Subnets $id deleted successfully!"
done


#detach internet gateway
internetgatewayId=$(aws ec2 describe-internet-gateways \
 --filters "Name=attachment.vpc-id,Values=$vpcId" | jq '.InternetGateways[].InternetGatewayId' | tr -d '"')

if [ -n "$internetgatewayId" ]
then
aws ec2 detach-internet-gateway \
 --internet-gateway-id $internetgatewayId \
 --vpc-id $vpcId
f
echo "Internet gateway detached successfully!"


#delete internet gateway
aws ec2 delete-internet-gateway \
 --internet-gateway-id $internetgatewayId
f
echo "Internet gateway deleted successfully!"
fi


#deleting route table
aws ec2 delete-route-table \
 --route-table-id $routetableId
f
echo "Route table deleted successfully!"


#delete vpc
aws ec2 delete-vpc --vpc-id $vpcId
f
echo "VPC deleted successfully!"


echo "Done"

sleep 2
clear
echo "You have delete VPC $vpcId successfully!"
# end of create-aws-vpc
