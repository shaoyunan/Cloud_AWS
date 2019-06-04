#!/bin/bash

echo "circleci-policy Stack deletion (detach ami policy from circleci user) in progress, please wait for completion"
aws cloudformation delete-stack --stack-name circleci-policy && aws cloudformation wait stack-delete-complete --stack-name circleci-policy || aws cloudformation describe-stack-events --stack-name circleci-policy | grep -B2 -A8 DELETE_FAILED
echo "circleci-policy Stack deletion complete"
