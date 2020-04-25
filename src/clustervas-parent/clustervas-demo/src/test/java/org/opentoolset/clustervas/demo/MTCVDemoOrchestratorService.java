// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.demo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentoolset.clustervas.demo.CVDemoOrchestratorApplication.Mode;
import org.opentoolset.clustervas.demo.service.CVDemoOrchestratorService;
import org.opentoolset.clustervas.sdk.NodeManagerContext;
import org.opentoolset.nettyagents.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MTCVDemoOrchestratorService {

	static {
		CVDemoOrchestratorApplication.mode = Mode.UNIT_TEST;
	}

	@Autowired
	private CVDemoOrchestratorService service;

	@Test
	public void test() throws IOException, InterruptedException {
		while (this.service.getNodeManagersWaiting().isEmpty()) {
			TimeUnit.SECONDS.sleep(1);
		}

		{
			List<NodeManagerContext> nodeManagers = this.service.getNodeManagersWaiting();
			String result = nodeManagers.stream().map(nm -> buildNodeManagerStr(nm)).collect(Collectors.joining("\n"));
			print(result);
		}
	}

	// ---

	private String buildNodeManagerStr(NodeManagerContext nodeManager) {
		String result = String.format("id: %s, fingerprint: %s", nodeManager.getPeerContext().getId(), Utils.getFingerprintAsHex(nodeManager.getPeerContext().getCert()));
		return result;
	}

	private void print(String format, Object... args) {
		System.out.printf(format, args);
	}
}
