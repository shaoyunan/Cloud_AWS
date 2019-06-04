#!/bin/bash

if [ -z "$1" ]; then
	echo "Please add your stack name as parameter"
else
	echo "$1 Stack deletion in progress, please wait for completion"
	aws cloudformation delete-stack --stack-name $1 && aws cloudformation wait stack-delete-complete --stack-name $1 || aws cloudformation describe-stack-events --stack-name $1 | grep -B2 -A8 DELETE_FAILED
	if [ $? -eq 0 ];then
		echo "$1 Stack deletion complete"
	fi
fi