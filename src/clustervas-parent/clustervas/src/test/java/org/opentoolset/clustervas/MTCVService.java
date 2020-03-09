// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentoolset.clustervas.CVContext;
import org.opentoolset.clustervas.CVContext.Mode;
import org.opentoolset.clustervas.api.messages.GvmCliRequest;
import org.opentoolset.clustervas.api.messages.GvmCliResponse;
import org.opentoolset.clustervas.api.messages.LoadNewNodeRequest;
import org.opentoolset.clustervas.api.messages.LoadNewNodeResponse;
import org.opentoolset.clustervas.api.messages.RemoveNodeRequest;
import org.opentoolset.clustervas.api.messages.RemoveNodeResponse;
import org.opentoolset.clustervas.service.CVService;
import org.opentoolset.clustervas.utils.CVLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
