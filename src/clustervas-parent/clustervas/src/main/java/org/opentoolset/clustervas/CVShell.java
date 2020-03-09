package org.opentoolset.clustervas;

import org.opentoolset.clustervas.service.CVAgent;
import org.opentoolset.clustervas.service.CVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class CVShell {

	private static final int MAX_HOST_LIMIT = 5000;

	@Autowired
	private CVService cvService;

	@Autowired
	private CVAgent agent;

	@ShellMethod("Generate key-pair")
	public void genKeys() throws Exception {

	}

	@ShellMethod("Mevcut bir musterinin lisansini yeniden uretir")
	public void renewLicense(String guid, int validMonthCount, @ShellOption(defaultValue = "-1") int hostLimit) {
		
		
	}

	// ------

}