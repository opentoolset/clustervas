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
import org.opentoolset.clustervas.service.ContainerService;
import org.opentoolset.clustervas.service.ContainerService.CVContainer;
import org.opentoolset.clustervas.utils.CVLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
	public void testSaveTemplateContainer() throws IOException {
		Assert.assertTrue(service.loadOperationalImageFromTemplateContainer());
	}

	@Test
	public void testInternalNVTSync() throws IOException {
		Assert.assertTrue(service.doInternalNVTSync());
	}

	@Test
	public void testLoadNode() throws IOException {
		CVContainer container = service.loadNewContainer();
		Assert.assertTrue(container != null);
		CVLogger.info(container.toString());
	}
}
