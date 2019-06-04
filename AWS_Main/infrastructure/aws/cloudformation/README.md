# CSYE 6225 - Spring 2019

## aws cli cloudformation scripts

### Usage
cd to script folder  
* use ./csye6225-aws-cf-create-stack.sh stackname to create a stack  
* use ./csye6225-aws-cf-terminate-stack.sh stackname to delete a stack

* use ./csye6225-aws-cf-create-application-stack.sh stackname stackname2, to create a stack, stackname2 is the reference of former stack which use to create new vpc, the second parameter is to import the information in that stack  
* use ./csye6225-aws-cf-terminate-application-stack.sh stackname to delete the stack and terminate ec2 instance  
  

*Cloud Formation template in csye6225-cf-networking.json, csye6225-cf-application.json  
*Please check the permission of files if access denied in terminal.
