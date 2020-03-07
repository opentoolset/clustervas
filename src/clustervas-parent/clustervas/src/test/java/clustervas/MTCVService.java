// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import clustervas.CVContext.Mode;
import clustervas.api.messages.GvmCliRequest;
import clustervas.api.messages.GvmCliResponse;
import clustervas.api.messages.LoadNewNodeRequest;
import clustervas.api.messages.LoadNewNodeResponse;
import clustervas.api.messages.RemoveNodeRequest;
import clustervas.api.messages.RemoveNodeResponse;
import clustervas.service.CVService;
import clustervas.utils.CVLogger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MTCVService {

	static {
		CVContext.mode = Mode.UNIT_TEST;
	}

	@Autowired
	private CVService service;

	@Test
	public void testGvmCommand() throws IOException {
		String nodeName = null;

		{
			LoadNewNodeResponse response = this.service.handle(new LoadNewNodeRequest());
			Assert.assertTrue(response.isSuccessfull());

			nodeName = response.getNodeName();
			CVLogger.info(nodeName);
		}

		try {
			GvmCliRequest gvmCliRequest = new GvmCliRequest();
			gvmCliRequest.setNodeName(nodeName);
			gvmCliRequest.setXml("<get_configs />");
			GvmCliResponse response = this.service.handle(gvmCliRequest);
			Assert.assertTrue(response.isSuccessfull());
			CVLogger.info(response.getXml());
		} finally {
			RemoveNodeRequest request = new RemoveNodeRequest();
			request.setNodeName(nodeName);
			RemoveNodeResponse response = this.service.handle(request);
			Assert.assertTrue(response.isSuccessfull());
		}
	}
}
