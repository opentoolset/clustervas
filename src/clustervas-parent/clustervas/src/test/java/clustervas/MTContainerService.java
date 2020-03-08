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
import clustervas.utils.CVLogger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MTContainerService {

	static {
		CVContext.mode = Mode.UNIT_TEST;
	}

	@Autowired
	private ContainerService service;

	@Test
	public void testLoadTemplateIfNeeded() throws IOException {
		boolean loaded = service.loadTemplateContainerIfNeeded();
		Assert.assertTrue(loaded);
	}

	@Test
	public void testSaveTemplate() throws IOException {
		Assert.assertTrue(service.loadTemplateContainerIfNeeded());
		Assert.assertTrue(service.saveClusterVASImage(() -> false));
	}

	@Test
	public void testInternalNVTSync() throws IOException {
		Assert.assertTrue(service.doInternalNVTSync());
	}

	@Test
	public void testLoadNode() throws IOException {
		CVContainer container = service.loadNewNodeContainer();
		Assert.assertTrue(container != null);
		CVLogger.info(container.toString());
	}
}
