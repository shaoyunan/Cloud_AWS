#!/bin/bash
# create-aws-vpc
# variables used in script:
# download jq: sudo apt-get install jq
clear
echo "Welcome!"

if [ ! -n "$1" ]
then
echo "Next time enter a string as your VPC name"
exit
fi

availabilityZone=(us-east-1a us-east-1b us-east-1c)
name="$1"
vpcName="$name VPC"
subnetName="$name Subnet"
gatewayName="$name Gateway"
routeTableName="$name Route Table"
securityGroupName="$name Security Group"
vpcCidrBlock="10.0.0.0/16"
subNetCidrBlock=(10.0.0.0/24 10.0.1.0/24 10.0.2.0/24)
port22CidrBlock="0.0.0.0/0"
destinationCidrBlock="0.0.0.0/0"

function f(){
if [ $? -ne 0 ]
then
echo "Goodby"
sleep 2
exit
fi
}

echo "Creating VPC..."

#create vpc with cidr block /16
aws_response=$(aws ec2 create-vpc \
 --cidr-block "$vpcCidrBlock" \
 --output json)
f
echo "VPC created successfully!"
vpcId=$(echo -e "$aws_response" |  jq '.Vpc.VpcId' | tr -d '"')

#name the vpc
aws ec2 create-tags \
  --resources "$vpcId" \
  --tags Key=Name,Value="$vpcName"

#add dns support
modify_response=$(aws ec2 modify-vpc-attribute \
 --vpc-id "$vpcId" \
 --enable-dns-support "{\"Value\":true}")

#add dns hostnames
modify_response=$(aws ec2 modify-vpc-attribute \
  --vpc-id "$vpcId" \
  --enable-dns-hostnames "{\"Value\":true}")

#create route table for vpc
route_table_response=$(aws ec2 create-route-table \
 --vpc-id "$vpcId" \
 --output json)
f
echo "Route table created successfully!"
routeTableId=$(echo -e "$route_table_response" |  jq '.RouteTable.RouteTableId' | tr -d '"')

#name the route table
aws ec2 create-tags \
  --resources "$routeTableId" \
  --tags Key=Name,Value="$routeTableName"

for k in $(seq 1 3)
do
subnetName[$k]="$name Subnet$k"
#create subnet for vpc with /24 cidr block
subnet_response=$(aws ec2 create-subnet \
 --cidr-block "${subNetCidrBlock[$k-1]}" \
 --availability-zone "${availabilityZone[$k-1]}" \
 --vpc-id "$vpcId" \
 --output json)
f
echo "Subnet$k created successfully!"
subnetId[$k]=$(echo -e "$subnet_response" |  jq '.Subnet.SubnetId' | tr -d '"')

#name the subnet
aws ec2 create-tags \
  --resources "${subnetId[$k]}" \
  --tags Key=Name,Value="${subnetName[$k]}"

#enable public ip on subnet
modify_response=$(aws ec2 modify-subnet-attribute \
 --subnet-id "${subnetId[$k]}" \
 --map-public-ip-on-launch)

#add route to subnet
associate_response=$(aws ec2 associate-route-table \
 --subnet-id "${subnetId[$k]}" \
 --route-table-id "$routeTableId")

done

#create internet gateway
gateway_response=$(aws ec2 create-internet-gateway \
 --output json)
f
echo "Internet gateway created successfully!"
gatewayId=$(echo -e "$gateway_response" |  jq '.InternetGateway.InternetGatewayId' | tr -d '"')

#name the internet gateway
aws ec2 create-tags \
  --resources "$gatewayId" \
  --tags Key=Name,Value="$gatewayName"

#attach gateway to vpc
attach_response=$(aws ec2 attach-internet-gateway \
 --internet-gateway-id "$gatewayId"  \
 --vpc-id "$vpcId")

#add route for the internet gateway
route_response=$(aws ec2 create-route \
 --route-table-id "$routeTableId" \
 --destination-cidr-block "$destinationCidrBlock" \
 --gateway-id "$gatewayId")

#find default security group id
groupId=$(aws ec2 describe-security-groups \
 --filter "Name=vpc-id,Values=$vpcId" | jq '.SecurityGroups[].GroupId' | tr -d '"')
f

#remove existing rule
aws ec2 revoke-security-group-ingress \
 --group-id $groupId \
 --source-group $groupId \
 --protocol all --port all
echo "Existing rules deleted successfully!"

#enable port 22
aws ec2 authorize-security-group-ingress \
 --group-id "$groupId" \
 --protocol tcp --port 22 \
 --cidr "$port22CidrBlock"

#enable port 80
aws ec2 authorize-security-group-ingress \
 --group-id "$groupId" \
 --protocol tcp --port 80 \
 --cidr "$port22CidrBlock"

echo "All set"
sleep 3
clear
echo "VPC id is: $vpcId"
echo "Use subnet id are: "
for id in ${subnetId[@]}
do
    echo "$id"
done
echo "Security group id is: $groupId"
# end of create-aws-vpc
