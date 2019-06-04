package com.csye6225.springbootapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.csye6225.springbootapi.dao.UserDAO;
import com.csye6225.springbootapi.pojo.User;
import com.timgroup.statsd.NonBlockingStatsDClient;

@RestController
public class ResetController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private NonBlockingStatsDClient client = new NonBlockingStatsDClient("csye6225.webapp", "localhost", 8125);

	@Autowired
	private UserDAO uDao;

	@Value("${domain.name}")
	private String domain;

	@RequestMapping(value = "/pwdreset", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<Object> resetPassword(@RequestParam("email") String email){
		
		client.incrementCounter("/reset | POST");
		
		User u = uDao.findOneByEmail(email);
		
		if(u!=null) {
			
			AmazonSNS sns = AmazonSNSClientBuilder
	                 .standard()
	                 .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
	                 .withRegion(Regions.US_EAST_1)
	                 .build();
		
			String message = domain+"/reset?email="+email+"&token=";
			
			ListTopicsResult ltr = sns.listTopics();
			
			for(Topic topic : ltr.getTopics()) {
				//LOGGER.info(topic.getTopicArn());
				if(topic.getTopicArn().endsWith("password_reset")) {
					PublishRequest request = new PublishRequest(topic.getTopicArn(), message);
					
					MessageAttributeValue mEmail =  new MessageAttributeValue()
			                .withDataType("String")
			                .withStringValue(email);
					MessageAttributeValue mDomain =  new MessageAttributeValue()
			                .withDataType("String")
			                .withStringValue(domain);
					
			        request.addMessageAttributesEntry("id", mEmail);
			        request.addMessageAttributesEntry("domain", mDomain);
			        
					PublishResult result = sns.publish(request);
					
					LOGGER.info("Password Reset for: "+email+". MessageID: "+result.getMessageId());
					
					return ResponseEntity.status(HttpStatus.CREATED).body("{\"Message\":\"Email Sent\"}");
				}
			}
		}
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Message\":\"Bad Request\"}");
	}

}
