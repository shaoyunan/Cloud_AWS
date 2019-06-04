#!/bin/bash
#CentOS AMI ENV Setup

#System Update
sudo yum -y update
sudo yum -y install wget
sudo yum -y install zip unzip
sudo yum -y install git
sudo yum -y install iptables-services
#curl -sL https://rpm.nodesource.com/setup_8.x | sudo bash -
#sudo yum -y install nodejs
sudo yum -y install ruby

#Java Installation
curl -L -b "oraclelicense=a" -O https://download.oracle.com/otn-pub/java/jdk/8u202-b08/1961070e4c9b4e26a04e7f5a083f551e/jdk-8u202-linux-x64.rpm
sudo yum -y localinstall jdk-8u202-linux-x64.rpm
sudo rm jdk-8u202-linux-x64.rpm

#Tomcat8.5
cd /tmp
wget http://www-us.apache.org/dist/tomcat/tomcat-8/v8.5.39/bin/apache-tomcat-8.5.39.zip

unzip apache-tomcat-*.zip
sudo mkdir -p /opt/tomcat
sudo mv apache-tomcat-8.5.39/* /opt/tomcat
sudo rm apache-tomcat-*.zip

#tomcat config
sudo groupadd tomcat
sudo useradd -M -s /bin/nologin -g tomcat -d /opt/tomcat tomcat
sudo chgrp -R tomcat /opt/tomcat
sudo chown -R tomcat /opt/tomcat
sudo chmod +x /opt/tomcat/bin/*.sh
sudo chmod -R 777 /opt/tomcat/webapps /opt/tomcat/temp /opt/tomcat/logs /opt/tomcat/work /opt/tomcat/conf

sudo touch /opt/tomcat/bin/setenv.sh
sudo chown tomcat:tomcat /opt/tomcat/bin/setenv.sh
sudo chmod +x /opt/tomcat/bin/setenv.sh
sudo echo 'export spring_config_location=/opt/tomcat/conf/' >> /opt/tomcat/bin/setenv.sh

sudo touch /opt/tomcat/conf/csye6225.properties
sudo chown tomcat:tomcat /opt/tomcat/conf/csye6225.properties
sudo echo 'controller.type = cloud' >> /opt/tomcat/conf/csye6225.properties
sudo echo 'logging.file = /opt/tomcat/logs/csye6225.log' >> /opt/tomcat/conf/csye6225.properties
#sudo echo 'management.metrics.export.statsd.enabled=true' >> /opt/tomcat/conf/csye6225.properties
#sudo echo 'management.metrics.export.statsd.host=localhost' >> /opt/tomcat/conf/csye6225.properties
#sudo echo 'management.metrics.export.statsd.port=8125' >> /opt/tomcat/conf/csye6225.properties
sudo echo 'instanceProfile=true' >> /opt/tomcat/conf/csye6225.properties
sudo echo 'amazonProperties.endpointUrl= https://s3.us-east-1.amazonaws.com' >> /opt/tomcat/conf/csye6225.properties
sudo echo 'spring.jpa.database-platform = org.hibernate.dialect.MySQL5Dialect' >> /opt/tomcat/conf/csye6225.properties
sudo echo 'spring.datasource.driver-class-name = com.mysql.jdbc.Driver' >> /opt/tomcat/conf/csye6225.properties
sudo echo 'spring.jpa.hibernate.ddl-auto = update' >> /opt/tomcat/conf/csye6225.properties

sudo touch /etc/systemd/system/tomcat.service

sudo echo '[Unit]' >>/etc/systemd/system/tomcat.service
sudo echo 'Description=Tomcat 8.5 servlet container' >>/etc/systemd/system/tomcat.service
sudo echo 'After=syslog.target network.target' >>/etc/systemd/system/tomcat.service
sudo echo '' >>/etc/systemd/system/tomcat.service
sudo echo '[Service]' >>/etc/systemd/system/tomcat.service
sudo echo 'Type=forking' >>/etc/systemd/system/tomcat.service
sudo echo '' >>/etc/systemd/system/tomcat.service
sudo echo 'User=tomcat' >>/etc/systemd/system/tomcat.service
sudo echo 'Group=tomcat' >>/etc/systemd/system/tomcat.service
sudo echo '' >>/etc/systemd/system/tomcat.service
sudo echo 'Environment=JAVA_HOME=/usr/java/jdk1.8.0_202-amd64' >>/etc/systemd/system/tomcat.service
sudo echo 'Environment="JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"' >>/etc/systemd/system/tomcat.service
sudo echo '' >>/etc/systemd/system/tomcat.service
sudo echo 'Environment=CATALINA_BASE=/opt/tomcat' >>/etc/systemd/system/tomcat.service
sudo echo 'Environment=CATALINA_HOME=/opt/tomcat' >>/etc/systemd/system/tomcat.service
sudo echo 'Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid' >>/etc/systemd/system/tomcat.service
sudo echo 'Environment="CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC"' >>/etc/systemd/system/tomcat.service
sudo echo '' >>/etc/systemd/system/tomcat.service
sudo echo 'ExecStart=/opt/tomcat/bin/startup.sh' >>/etc/systemd/system/tomcat.service
sudo echo 'ExecStop=/opt/tomcat/bin/shutdown.sh' >>/etc/systemd/system/tomcat.service
sudo echo 'ExecReload=/usr/bin/kill -s HUP $MAINPID' >>/etc/systemd/system/tomcat.service
sudo echo '' >>/etc/systemd/system/tomcat.service
sudo echo 'UMask=0007' >>/etc/systemd/system/tomcat.service
sudo echo 'RestartSec=10' >>/etc/systemd/system/tomcat.service
sudo echo 'Restart=always' >>/etc/systemd/system/tomcat.service
sudo echo '' >>/etc/systemd/system/tomcat.service
sudo echo '[Install]' >>/etc/systemd/system/tomcat.service
sudo echo 'WantedBy=multi-user.target' >>/etc/systemd/system/tomcat.service

#CloudWatch Agent
cd /tmp
wget https://s3.amazonaws.com/amazoncloudwatch-agent/centos/amd64/latest/amazon-cloudwatch-agent.rpm
sudo rpm -U ./amazon-cloudwatch-agent.rpm
sudo rm amazon-cloudwatch-agent.rpm

#statsd
#cd /opt
#git clone https://github.com/etsy/statsd.git
#cd statsd
#sudo touch config.js
#sudo chmod 777 config.js
#sudo echo '{port: 8125,mgmt_port: 8126,backends: [ "./backends/console" ]}' >>config.js

#sudo npm install -g forever
#sudo npm install -g forever-service
#sudo forever-service install statsd -s stats.js -o " config.js"

#CodeDeploy Agent
sudo mkdir /usr/bin/ec2-user
cd /usr/bin/ec2-user
wget https://aws-codedeploy-us-east-1.s3.amazonaws.com/latest/install
sudo chmod +x ./install
sudo ./install auto
