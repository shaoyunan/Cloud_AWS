#!/bin/bash

if [ -z "$1" ]; then
	echo "Please add your stack name as parameter"
else
	
	#out=$(aws cloudformation create-stack --stack-name $1 --template-body file://csye6225-cf-networking.json --parameters ParameterKey=stackname,ParameterValue=$1)
	echo "$1 Stack creation in progress, please wait for completion"
	#aws cloudformation wait stack-create-complete --stack-name $1
	aws cloudformation create-stack --stack-name $1 --template-body file://csye6225-cf-networking.json --parameters ParameterKey=stackname,ParameterValue=$1 && aws cloudformation wait stack-create-complete --stack-name $1 || aws cloudformation describe-stack-events --stack-name $1 | grep -B2 -A8 CREATE_FAILED
	
	if [ $? -eq 0 ];then
		status=$(aws cloudformation describe-stacks --stack-name  $1| grep StackStatus| cut -d'"' -f4)
		if [ "$status" == "CREATE_COMPLETE" ];then
			echo "$1 Stack creation complete"
			echo $(aws cloudformation describe-stacks --stack-name $1)
		fi
	fi
fi

