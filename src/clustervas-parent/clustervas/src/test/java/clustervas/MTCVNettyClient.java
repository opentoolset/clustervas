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
import clustervas.api.messages.SampleRequest1;
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

	@Autowired
	private CVAgent cvAgent;

	@BeforeClass
	public static void beforeClass() throws Exception {
		cvManagerAgent = new CVManagerAgent();
	}

	@Test
	public void testCommunication() throws Exception {
		cvAgent.startup();
		cvManagerAgent.startup();

		SampleResponse sampleResponse = cvManagerAgent.getContext().getMessageSender().doRequest(new SampleRequest1(), SampleResponse.class);
		Assert.assertNotNull(sampleResponse);

		cvManagerAgent.shutdown();
		cvAgent.shutdown();
	}
}
