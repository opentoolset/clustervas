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
import clustervas.api.netty.agent.CVClientAgent;
import clustervas.service.netty.CVServerAgent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MTCVNettyClient {

	static {
		CVContext.mode = Mode.UNIT_TEST;
	}

	private static CVClientAgent connectionAcceptor;
	private static CVServerServiceConsumer cvServiceConsumer;

	@Autowired
	private CVServerAgent connectionInitiator;

	@BeforeClass
	public static void beforeClass() throws Exception {
		connectionAcceptor = new CVClientAgent();
		new Thread(() -> connectionAcceptor.run()).start();

		cvServiceConsumer = new CVServerServiceConsumer();
	}

	@Test
	public void testCommunication() throws Exception {
		Assert.assertTrue(connectionInitiator.openChannel());

		SampleRequest sampleRequest = new SampleRequest();
		SampleResponse sampleResponse = cvServiceConsumer.getSampleResponse(sampleRequest);
		Assert.assertNotNull(sampleResponse);
	}

	private static class CVServerServiceConsumer implements CVServerService {

		@Override
		public SampleResponse getSampleResponse(SampleRequest request) {
			return connectionAcceptor.doRequest(request, SampleResponse.class);
		}
	}
}
