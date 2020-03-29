package org.opentoolset.clustervas;

import org.opentoolset.clustervas.service.CVManagerAgent;
import org.opentoolset.clustervas.service.CVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class CVShell {

	@Autowired
	private CVService cvService;

	@Autowired
	private CVManagerAgent agent;

	@ShellMethod("Generate key-pair")
	public void genKeys() throws Exception {

	}

	// ------
}