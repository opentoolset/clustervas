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
	public void test() throws Exception {
		serverAgent.startup();
		clientAgent1.startup();
		clientAgent2.startup();

		Map<SocketAddress, PeerContext> clients = null;
		while (true) {
			clients = serverAgent.getClients();
			if (clients.size() > 1) {
				break;
			} else {
				TimeUnit.SECONDS.sleep(1);
			}
		}

		Iterator<PeerContext> iterator = clients.values().iterator();
		PeerContext client1 = iterator.next();
		PeerContext client2 = iterator.next();

		{
			clientAgent1.sendMessage(new SampleMessage("Sample message from client-1"));
			clientAgent2.sendMessage(new SampleMessage("Sample message from client-2"));

			{
				SampleResponse response = clientAgent1.doRequest(new SampleRequest("Sample request from client-1"));
				System.out.printf("Response received from server: %s\n", response);
				Assert.assertNotNull(response);
			}

			{
				SampleResponse response = clientAgent2.doRequest(new SampleRequest("Sample request from client-2"));
				System.out.printf("Response received from server: %s\n", response);
				Assert.assertNotNull(response);
			}
		}

		{
			serverAgent.sendMessage(new SampleMessage("Sample message from server to client1"), client1);
			serverAgent.sendMessage(new SampleMessage("Sample message from server to client2"), client2);

			{
				SampleResponse response = serverAgent.doRequest(new SampleRequest("Sample request from server to client-1"), client1);
				System.out.printf("Response received from client: %s\n", response);
				Assert.assertNotNull(response);
			}

			{
				SampleResponse response = serverAgent.doRequest(new SampleRequest("Sample request from server to client-2"), client2);
				System.out.printf("Response received from client: %s\n", response);
				Assert.assertNotNull(response);
			}
		}

		clientAgent1.shutdown();
		clientAgent2.shutdown();
		serverAgent.shutdown();
	}

	// ---

	@BeforeClass
	public static void beforeClass() throws Exception {
		serverAgent = new ServerAgent();
		serverAgent.setMessageHandler(SampleMessage.class, message -> handleMessageOnServer(message));
		serverAgent.setRequestHandler(SampleRequest.class, request -> handleRequestOnServer(request));

		clientAgent1 = new ClientAgent();
		clientAgent1.setMessageHandler(SampleMessage.class, message -> handleMessageOnClient1(message));
		clientAgent1.setRequestHandler(SampleRequest.class, request -> handleRequestOnClient1(request));

		clientAgent2 = new ClientAgent();
		clientAgent2.setMessageHandler(SampleMessage.class, message -> handleMessageOnClient2(message));
		clientAgent2.setRequestHandler(SampleRequest.class, request -> handleRequestOnClient2(request));
	}

	private static void handleMessageOnServer(SampleMessage message) {
		System.out.printf("Message received on server: %s\n", message);
	}

	private static SampleResponse handleRequestOnServer(SampleRequest request) {
		System.out.printf("Request received on server: %s\n", request);
		SampleResponse response = new SampleResponse("Sample response from server");
		System.out.printf("Response sending on server: %s\n", response);
		return response;
	}

	private static void handleMessageOnClient1(SampleMessage message) {
		System.out.printf("Message received on client1: %s\n", message);
	}

	private static SampleResponse handleRequestOnClient1(SampleRequest request) {
		System.out.printf("Request received on client1: %s\n", request);
		SampleResponse response = new SampleResponse("Sample response from client1");
		System.out.printf("Response sending on client1: %s\n", response);
		return response;
	}

	private static void handleMessageOnClient2(SampleMessage message) {
		System.out.printf("Message received on client2: %s\n", message);
	}

	private static SampleResponse handleRequestOnClient2(SampleRequest request) {
		System.out.printf("Request received on client2: %s\n", request);
		SampleResponse response = new SampleResponse("Sample response from client2");
		System.out.printf("Response sending on client2: %s\n", response);
		return response;
	}
}
