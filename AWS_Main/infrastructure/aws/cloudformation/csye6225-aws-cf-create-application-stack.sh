#!/bin/bash

rdsuser=csye6225master
rdspwd=csye6225password
#awsregion = us-east-1

if [ -z "$1" ] ; then
	echo "Please add a stack name as parameter"
else
if [ -z "$2" ] ; then
	echo "Please add another stack name to refer"csye6225-spring2019-ami
else

usrid=$(aws sts get-caller-identity --output json | jq '.Account' | tr -d '"')
amis=$(aws ec2 describe-images --owner $usrid --query 'sort_by(Images, &CreationDate)[-1].ImageId' --output text)
amiid=${amis}
echo "Latest AMI ID: $amiid"

domain=$(aws route53 list-hosted-zones --query HostedZones[0].Name --output text)
s3name="${domain::-1}.csye6225.com"
echo "DB S3Bucket Name: $s3name"

domainName=${domain::-1}
echo "Domain Name: $domainName"

#deploys3=$(aws s3api list-buckets --query "Buckets[].Name" --output text | grep "code-deploy" | tr -d '"' | tr -d ',')
deploys3="code-deploy.${domain::-1}"
echo "Code Deploy S3Bucket Name: $deploys3"

#lambda=$(aws lambda get-function --function-name "lambda-reset" --query "Configuration.FunctionArn" --output text)
#echo $lambda

echo "$1 Stack creation in progress, please wait for completion"
aws cloudformation create-stack \
 --stack-name $1 \
 --template-body file://csye6225-cf-application.json \
 --capabilities CAPABILITY_NAMED_IAM \
 --parameters ParameterKey=stackname,ParameterValue=$1 ParameterKey=subnet1ID,ParameterValue=$2-subnet1ID ParameterKey=subnet2ID,ParameterValue=$2-subnet2ID ParameterKey=subnet3ID,ParameterValue=$2-subnet3ID ParameterKey=vpcID,ParameterValue=$2-vpcID ParameterKey=amiID,ParameterValue=$amiid ParameterKey=s3Name,ParameterValue=$s3name ParameterKey=RDSUser,ParameterValue=$rdsuser ParameterKey=RDSPassword,ParameterValue=$rdspwd ParameterKey=DeployS3,ParameterValue=$deploys3 ParameterKey=AWSID,ParameterValue=$usrid ParameterKey=DomainName,ParameterValue=$domainName && aws cloudformation wait stack-create-complete --stack-name $1 || aws cloudformation describe-stack-events --stack-name $1 | grep -B2 -A8 CREATE_FAILED
	
	if [ $? -eq 0 ];then
		echo "Updating Lambda Function Configuration"
		aws lambda update-function-configuration --function-name LambdaReset --runtime java8

		status=$(aws cloudformation describe-stacks --stack-name  $1| grep StackStatus| cut -d'"' -f4)
		if [ "$status" == "CREATE_COMPLETE" ];then
			echo "$1 Stack creation complete"
			echo $(aws cloudformation describe-stacks --stack-name $1)
			echo ""
			echo "Please wait for 5 minutes for user data to set up application environment. Then check your EC2 endpoint to see if tomcat is up and running"
			echo "Use CircleCI console or direct API call to do the first deployment after confirmation."
		fi
	fi
fi
fi

