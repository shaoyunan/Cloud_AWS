package com.csye6225.awslambda;

import java.util.Iterator;
import java.util.UUID;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

public class PasswordReset implements RequestHandler<SNSEvent, Object> {

	public Object handleRequest(SNSEvent input, Context context) {

		String email = "";
		String message = "";
		String domain = "";

		SNS sns = input.getRecords().get(0).getSNS();

		message = sns.getMessage();
		email = sns.getMessageAttributes().get("id").getValue();
		domain = sns.getMessageAttributes().get("domain").getValue();

		// AmazonDynamoDB client =
		// AmazonDynamoDBClientBuilder.standard().withCredentials(new
		// DefaultAWSCredentialsProviderChain()).withRegion(Regions.US_EAST_1).build();
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		DynamoDB db = new DynamoDB(client);
		Table table = db.getTable("csye6225");
		Long cTime = System.currentTimeMillis() / 1000;

		QuerySpec query = new QuerySpec().withKeyConditionExpression("id = :ee and expirettl > :tt")
				.withValueMap(new ValueMap().withString(":ee", email).withNumber(":tt", cTime));

		ItemCollection<QueryOutcome> items = table.query(query);
		Iterator<Item> iterator = items.iterator();

		if (!iterator.hasNext()) {
			Number ttl = cTime + 60;
			String uuid = UUID.randomUUID().toString();
			table.putItem(new PutItemSpec().withItem(
					new Item().withString("id", email).withString("token", uuid).withNumber("expirettl", ttl)));
			try {
				message = message + uuid;
				AmazonSimpleEmailService client_email = AmazonSimpleEmailServiceClientBuilder.standard()
						.withRegion(Regions.US_EAST_1).build();
				SendEmailRequest request = new SendEmailRequest()
						.withDestination(new Destination().withToAddresses(email))
						.withMessage(new Message()
								.withBody(new Body().withText(new Content().withCharset("UTF-8").withData(message)))
								.withSubject(new Content().withCharset("UTF-8").withData("Password Reset Request")))
						.withSource("noreply@" + domain);

				context.getLogger().log("Email Sent. Email: " + email);
				client_email.sendEmail(request);
			} catch (Exception ex) {
				System.out.println("Request Failed. Error message: " + ex.getMessage());
			}
		} else {
			context.getLogger().log("Active Request Found, Email will not be sent.");
		}
		return null;
	}

}
