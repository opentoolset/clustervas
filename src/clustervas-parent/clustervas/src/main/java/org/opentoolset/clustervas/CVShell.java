// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import org.opentoolset.clustervas.service.CVNodeManager;
import org.opentoolset.clustervas.service.CVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class CVShell {

	@Autowired
	private CVService cvService;

	@Autowired
	private CVNodeManager agent;

	@ShellMethod("Generate key-pair")
	public void genKeys() throws Exception {

	}

	// ------
}