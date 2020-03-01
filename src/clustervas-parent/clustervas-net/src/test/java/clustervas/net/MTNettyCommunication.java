// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MTNettyCommunication {

	private static ServerAgent serverAgent;
	private static ClientAgent clientAgent;

	@BeforeClass
	public static void beforeClass() throws Exception {
		serverAgent = new ServerAgent();
		MessageReceiver messageReceiver = serverAgent.getContext().getMessageReceiver();
		messageReceiver.setMessageHandler(SampleMessage.class, message -> System.out.printf("Message received: %s\n", message));
		messageReceiver.setRequestHandler(SampleRequest.class, request -> handleRequest(request));

		clientAgent = new ClientAgent();
	}

	private static SampleResponse handleRequest(SampleRequest request) {
		System.out.printf("Request received: %s\n", request);
		SampleResponse response = new SampleResponse();
		System.out.printf("Response sending: %s\n", response);
		return response;
	}

	@Test
	public void testCommunication() throws Exception {
		serverAgent.startup();
		clientAgent.startup();

		clientAgent.getContext().getMessageSender().sendMessage(new SampleMessage());

		SampleResponse response = clientAgent.getContext().getMessageSender().doRequest(new SampleRequest());
		System.out.printf("Response received: %s\n", response);
		Assert.assertNotNull(response);

		clientAgent.shutdown();
		serverAgent.shutdown();
	}

	// ---

	private static class SampleMessage extends AbstractMessage {

		public String text = "This is a sample message";

		@SuppressWarnings("unused")
		public String getText() {
			return text;
		}
	}

	private static class SampleRequest extends AbstractRequest<SampleResponse> {

		private String text = "This is a sample request";

		@Override
		public Class<SampleResponse> getResponseClass() {
			return SampleResponse.class;
		}

		@SuppressWarnings("unused")
		public String getText() {
			return text;
		}
	}

	private static class SampleResponse extends AbstractMessage {

		private String text = "This is a sample response";

		@SuppressWarnings("unused")
		public String getText() {
			return text;
		}
	}
}
