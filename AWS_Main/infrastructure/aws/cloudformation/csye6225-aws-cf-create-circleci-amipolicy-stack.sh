 #!/bin/bash

 echo "circleci-policy Stack creation (attach ami policy to circleci user) in progress, please wait for completion"
 aws cloudformation create-stack \
  --stack-name circleci-policy \
  --template-body file://csye6225-cf-circleci.json \
  --capabilities CAPABILITY_NAMED_IAM && aws cloudformation wait stack-create-complete --stack-name circleci-policy || aws cloudformation describe-stack-events --stack-name circleci-policy | grep -B2 -A8 CREATE_FAILED
  echo "circleci-policy Stack creation complete"