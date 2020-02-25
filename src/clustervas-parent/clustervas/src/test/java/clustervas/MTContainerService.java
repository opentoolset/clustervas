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
import clustervas.service.ContainerService;
import clustervas.service.ContainerService.CVContainer;
import clustervas.utils.Logger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MTContainerService {

	static {
		CVContext.mode = Mode.UNIT_TEST;
	}

	@Autowired
	private ContainerService service;

	@Test
	public void testLoadTemplate() throws IOException {
		boolean loaded = service.loadTemplateContainer();
		Assert.assertTrue(loaded);
	}

	@Test
	public void testSaveTemplate() throws IOException {
		Assert.assertTrue(service.loadTemplateContainer());
		Assert.assertTrue(service.saveClusterVASImage(() -> false));
	}

	@Test
	public void testLoadNode() throws IOException {
		CVContainer container = service.loadNewNodeContainer();
		Assert.assertTrue(container != null);
		Logger.info(container.toString());
	}
}
