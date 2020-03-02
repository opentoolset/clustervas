// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import clustervas.net.TestData.SampleMessage;
import clustervas.net.TestData.SampleRequest;
import clustervas.net.TestData.SampleResponse;

public class MTNettyMultiClient {

	private static ServerAgent serverAgent;
	private static ClientAgent clientAgent1;
	private static ClientAgent clientAgent2;

	// ---

	@Test
	public void testCommunication() throws Exception {
		serverAgent.startup();
		clientAgent1.startup();
		clientAgent2.startup();

		Map<SocketAddress, PeerContext> clients = null;
		while (true) {
			clients = serverAgent.getClients();
			if (clients.size() > 2) {
				break;
			} else {
				TimeUnit.SECONDS.sleep(1);
			}
		}

		Iterator<PeerContext> iterator = clients.values().iterator();
		PeerContext client1 = iterator.next();
		PeerContext client2 = iterator.next();

		clientAgent1.sendMessage(new SampleMessage("Sample message from client-1"));
		clientAgent2.sendMessage(new SampleMessage("Sample message from client-2"));

		{
			SampleResponse response = clientAgent1.doRequest(new SampleRequest("Sample request from client-1"));
			System.out.printf("Response received: %s\n", response);
			Assert.assertNotNull(response);
		}

		{
			SampleResponse response = clientAgent2.doRequest(new SampleRequest("Sample request from client-2"));
			System.out.printf("Response received: %s\n", response);
			Assert.assertNotNull(response);
		}

		clientAgent1.shutdown();
		clientAgent2.shutdown();
		serverAgent.shutdown();
	}

	// ---

	@BeforeClass
	public static void beforeClass() throws Exception {
		serverAgent = new ServerAgent();
		serverAgent.setMessageHandler(SampleMessage.class, message -> handleMessage(message));
		serverAgent.setRequestHandler(SampleRequest.class, request -> handleRequest(request));

		clientAgent1 = new ClientAgent();
		clientAgent2 = new ClientAgent();
	}

	private static void handleMessage(SampleMessage message) {
		System.out.printf("Message received: %s\n", message);
	}

	private static SampleResponse handleRequest(SampleRequest request) {
		System.out.printf("Request received: %s\n", request);
		SampleResponse response = new SampleResponse(String.format("Sample response to request: %s", request));
		System.out.printf("Response sending: %s\n", response);
		return response;
	}
}
