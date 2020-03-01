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
import clustervas.net.ServerAgent;
import clustervas.service.CVAgent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MTCVNettyClient {

	static {
		CVContext.mode = Mode.UNIT_TEST;
	}

	private static ServerAgent managerAgent;

	@Autowired
	private CVAgent cvAgent;

	@BeforeClass
	public static void beforeClass() throws Exception {
		managerAgent = new ServerAgent();
	}

	@Test
	public void testCommunication() throws Exception {
		cvAgent.startup();
		managerAgent.startup();

		SampleResponse sampleResponse = managerAgent.getContext().getMessageSender().doRequest(new SampleRequest1());
		Assert.assertNotNull(sampleResponse);

		managerAgent.shutdown();
		cvAgent.shutdown();
	}
}
