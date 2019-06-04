# AWS AMI for CSYE 6225

## Team Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
| Yunan Shao | 1818832 | shao.yun@husky.neu.edu |
| Haowen Chang | 1497263 | chang.haow@husky.neu.edu |
| Andong Wang | 1407537 | wang.ando@husky.neu.edu |
| Zeyu Huang | 1406989 | huang.zey@husky.neu.edu |

## Validate Template

```
packer validate centos-ami-template.json
```

## Validate Template

```
packer validate centos-ami-template.json
```

## Build AMI for centos

```
./packer build -var 'aws_access_key=REDACTED' -var 'aws_secret_key=REDACTED' -var 'aws_region=us-east-1' -var 'subnet_id=REDACTED' centos-ami-template.json 
```

## CI/CD
* Pipeline follows CircleCI
* Make sure there is a VPC already up and runnig before using, the script in CircleCI will automatically query for a subnet for Packer
1. Setting environment variables in AMI repo (names need to be exact as following because it's default aws credentials for circleci dockers)
    - AWS_ACCESS_KEY_ID : circleci access key
	- AWS_SECRET_ACCESS_KEY : circleci secret key
	- AWS_DEFAULT_REGION : us-east-1
2. Push to repo or Use API call to trigger CircleCI build
```
curl -u ${CIRCLE_API_USER_TOKEN} \
-d build_parameters[CIRCLE_JOB]=build \
https://circleci.com/api/v1.1/project/github/<YOUR_GITHUB_USERNAME>/<YOUR_REPO_NAME>/tree/<BRANCH>
```
### Reference: Tomcat on CentOS Tutorial (centos-ami-template already has tomcat installation)
1. https://www.howtoforge.com/tutorial/how-to-install-tomcat-on-centos/
2. https://linuxize.com/post/how-to-install-tomcat-8-5-on-centos-7/