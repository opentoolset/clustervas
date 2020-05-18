// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentoolset.clustervas.CVContext.Mode;
import org.opentoolset.clustervas.sdk.messages.GMPRequest;
import org.opentoolset.clustervas.sdk.messages.GMPResponse;
import org.opentoolset.clustervas.sdk.messages.LoadNewContainerRequest;
import org.opentoolset.clustervas.sdk.messages.LoadNewContainerResponse;
import org.opentoolset.clustervas.sdk.messages.RemoveContainerRequest;
import org.opentoolset.clustervas.sdk.messages.RemoveContainerResponse;
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
			LoadNewContainerResponse response = this.service.handle(new LoadNewContainerRequest());
			Assert.assertTrue(response.isSuccessfull());

			nodeName = response.getContainerName();
			CVLogger.info(nodeName);
		}

		try {
			GMPRequest gvmCliRequest = new GMPRequest();
			gvmCliRequest.setContainerName(nodeName);
			gvmCliRequest.setXml("<get_configs />");
			GMPResponse response = this.service.handle(gvmCliRequest);
			Assert.assertTrue(response.isSuccessfull());
			CVLogger.info(response.getXml());
		} finally {
			RemoveContainerRequest request = new RemoveContainerRequest();
			request.setContainerName(nodeName);
			RemoveContainerResponse response = this.service.handle(request);
			Assert.assertTrue(response.isSuccessfull());
		}
	}
}
