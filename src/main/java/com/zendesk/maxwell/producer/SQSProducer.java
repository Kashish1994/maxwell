package com.zendesk.maxwell.producer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.zendesk.maxwell.MaxwellContext;
import com.zendesk.maxwell.row.RowMap;

public class SQSProducer extends AbstractProducer {

	public SQSProducer(MaxwellContext context) {
		super(context);
	}

	private static String myQueueUrl = SQSProducerCredentials.getQueueName();
	private static AmazonSQS sqs;

	static {
		init();
	}

	private static void init() {

		AWSCredentials credentials = null;
		try {
			credentials = new SQSProducerCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}

		sqs = new AmazonSQSClient(credentials);
		Region usEast1 = Region.getRegion(Regions.AP_SOUTHEAST_1);
		sqs.setRegion(usEast1);

		System.out.println("===========================================");
		System.out.println("Getting Started with Amazon SQS");
		System.out.println("===========================================\n");

	}

	private static void msg(String msg) {
		try {
			if (msg == null) {
				return;
			}

			// Send a message
			System.out.println("Sending a message to MyQueue. " + myQueueUrl);
			System.out.println("Message Content " + msg);
			SendMessageRequest message = new SendMessageRequest();
			message.setQueueUrl(myQueueUrl);
			message.setMessageBody(msg);
			message.setDelaySeconds(0);
			SendMessageResult response = sqs.sendMessage(message);
			System.out.println("Response From SQS " + response);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SQS, such as not "
					+ "being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	@Override
	public void push(RowMap r) throws Exception {
		String output = r.toJSON(outputConfig);

		if (output != null)
			msg(output);
		this.context.setPosition(r);
	}
}
