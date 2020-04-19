// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.opentoolset.clustervas.service.CVNodeManager;
import org.opentoolset.clustervas.service.CVService;
import org.opentoolset.clustervas.utils.Utils;
import org.opentoolset.nettyagents.PeerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import jline.console.ConsoleReader;

@ShellComponent
public class CVShell {

	@Autowired
	private CVService cvService;

	@Autowired
	private CVNodeManager agent;

	private ConsoleReader consoleReader;

	@ShellMethod("Connect to an orchestrator")
	public void connect() throws Exception {
		String host = getInput("Enter orchestrator's IP or hostname (0 to exit):", input -> StringUtils.isNotBlank(input));
		if (host.equals("0")) {
			return;
		}

		Function<String, Integer> parser = (input) -> Integer.parseInt(input);
		String portStr = getInput("Enter orchestrator's port (0 to exit):", (input) -> Utils.noExcepion(() -> parser.apply(input)));
		if (host.equals("0")) {
			return;
		}

		int port = parser.apply(portStr);

		agent.shutdown();
		agent.getConfig().setRemoteHost(host);
		agent.getConfig().setRemotePort(port);
		agent.startPeerIdentificationMode();
		agent.startup();

		Utils.waitUntil(() -> agent.getServer() != null, 10);
		PeerContext server = agent.getServer();
		if (server == null) {
			println("The orchestrator couln't be connected. Please retry.");
			return;
		}

		X509Certificate orchestratorCert = server.getCert();
		String fingerprint = org.opentoolset.nettyagents.Utils.getFingerprintAsHex(orchestratorCert);

		println("The orchestrator's fingerprint is: %s", fingerprint);

		String trustOrNot = getInput("Trust (Y) or not (N)?", input -> input.matches("[ynYN]"));
		switch (trustOrNot) {
			case "Y":
				agent.setTrusted(server, fingerprint, orchestratorCert);
				agent.stopPeerIdentificationMode();
				break;

			default:
				break;
		}
	}

	// ------

	@PostConstruct
	private void postConstruct() throws IOException {
		this.consoleReader = new ConsoleReader();
	}

	private String getInput(String prompt, Predicate<String> validator) throws IOException {
		String input;
		while (true) {
			input = this.consoleReader.readLine(prompt);
			try {
				if (validator.test(input)) {
					break;
				}
			} catch (Exception e) {
			}
		}

		return input;
	}

	private void println(String format, Object... args) {
		print(format + "\n", args);
	}

	private void print(String format, Object... args) {
		System.out.printf(format, args);
	}
}