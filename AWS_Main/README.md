# CSYE 6225 - Spring 2019

## Team Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
| Yunan Shao | 1818832 | shao.yun@husky.neu.edu |
| Haowen Chang | 1497263 | chang.haow@husky.neu.edu |
| Andong Wang | 1407537 | wang.ando@husky.neu.edu |
| Zeyu Huang | 1406989 | huang.zey@husky.neu.edu |

## Technology Stack
Spring Boot

Hibernate

MySQL/MariaDB 10.3 

## Build Instructions
-Java 1.8  
-Apache Tomcat 8.5  
-MariaDB 10.3  
-eclipse  
~-Postman  

## Local Deploy Instructions
1. Install MariaDB 10.3[Installation](https://computingforgeeks.com/install-mariadb-10-on-ubuntu-18-04-and-centos-7/)
2. Add Tomcat folder path to servers if no existing server in eclipse
3. Use maven build or maven-update project
4. Run MainApplication as java application to start spring boot api (use external property file with following command)
```
--spring.config.location=/<SYS_FILE_PATH>/application.properties
```
5. Use Postman to test api end points at localhost:8080

## AWS Cloud Deploy Instructions
1. Create a network stack
2. Build an AMI (script is available in AMI Repo)
3. Create an application stack
4. Deploy to EC2 Instance (See CI/CD Section)

## Running Tests
Unit Test Cases use JUnit and mockMVC  
Run ControllerTest.java in test folder will run the existing 3 test cases
1. Test get method to "/" without header will get not logged in message
2. ~Test email format validation (Disabled after adding basic auth)~
3. ~Test password validation (Disabled after adding basic auth)~

## CI/CD
* CircleCI
1. Create VPC Stack - CloudFormation
	- Create: ./csye6225-aws-cf-create-stack.sh <NETWORK_STACK_NAME>
	- Terminate: ./csye6225-aws-cf-terminate-stack.sh <NETWORK_STACK_NAME>

2. Attach AMI policy to circleci user - CloudFormation
	- Create: ./csye6225-aws-cf-create-circleci-amipolicy-stack.sh	
	- Terminate: ./csye6225-aws-cf-terminate-circleci-amipolicy-stack.sh

3. Build AMI (See AMI Repo)

4. Create Application Stack (On Latest Created AMI) - CloudFormation
	- Create: ./csye6225-aws-cf-create-application-stack.sh <APPLICATION_STACK_NAME> <NETWORK_STACK_NAME>
	- Terminate : ./csye6225-aws-cf-terminate-application-stack.sh <APPLICATION_STACK_NAME>
    - Use CircleCI API for first deployment
    ```
    curl -u ${CIRCLE_API_USER_TOKEN} \
				 -d build_parameters[CIRCLE_JOB]=build \
				 https://circleci.com/api/v1.1/project/github/<YOUR_GITHUB_USERNAME>/csye6225-spring2019/tree/<branch>
    ```

5. CircleCI Environment Variables
    - AWS_ACCESS_KEY_ID : CIRCLECI_USER_ACCESS_KEY
	- AWS_SECRET_ACCESS_KEY : CIRCLECI_USER_SECRET_KEY
	- DEPLOY_BUCKET_NAME : CIRCLECI_CODE_DEPLOY_S3_BUCKET_NAME
	- AWS_DEFAULT_REGION : us-east-1

* Deploy through aws cli
```
aws deploy create-deployment \
			--application-name csye6225-webapp \
			--deployment-group-name csye6225-webapp-deployment \
			--s3-location bucket="${DEPLOY_BUCKET_NAME}",key="csye6225-web-app-${CIRCLE_BUILD_NUM}.zip",bundleType=zip
```

## Additional Config
1. Recommand to create a user for operations to S3, Check the first part of the [Guide](https://medium.com/oril/uploading-files-to-aws-s3-bucket-using-spring-boot-483fcb6f8646)
2. Modify run configuration in eclipse with argument `--spring.config.location=your/ absolute/path/application.properties` to use external property file which contains your aws db and s3 credentials
3. Format of local/cloud storage and db configurations are inside application.properties, the default configurations are set to local without overriding with external property file
4. ~use `git update-index --assume-unchanged webapp/springbootapi/src/main/resources/application.properties` to stop tracking the application.properties file after adding your s3 credentials~