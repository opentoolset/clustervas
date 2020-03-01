// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import clustervas.CVContext.Mode;
import clustervas.api.CVServerService;
import clustervas.api.messages.SampleRequest;
import clustervas.api.messages.SampleResponse;
import clustervas.api.netty.agent.CVManagerAgent;
import clustervas.service.netty.CVAgent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MTCVNettyClient {

	static {
		CVContext.mode = Mode.UNIT_TEST;
	}

	private static CVManagerAgent cvManagerAgent;
	private static CVServerServiceConsumer serverServiceConsumer;

	@Autowired
	private CVAgent cvAgent;

	@BeforeClass
	public static void beforeClass() throws Exception {
		cvManagerAgent = new CVManagerAgent();
		serverServiceConsumer = new CVServerServiceConsumer();
	}

	@Test
	public void testCommunication() throws Exception {
		new Thread(() -> cvManagerAgent.startup()).start();
		new Thread(() -> cvAgent.startup()).start();

		SampleRequest sampleRequest = new SampleRequest();
		SampleResponse sampleResponse = serverServiceConsumer.getSampleResponse(sampleRequest);

		cvManagerAgent.shutdown();
		cvAgent.shutdown();

		Assert.assertNotNull(sampleResponse);
	}

	private static class CVServerServiceConsumer implements CVServerService {

		@Override
		public SampleResponse getSampleResponse(SampleRequest request) {
			return cvManagerAgent.doRequest(request, SampleResponse.class);
		}
	}
}
