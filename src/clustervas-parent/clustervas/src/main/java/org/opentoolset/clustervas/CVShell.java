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

@ShellComponent(value = "ClusterVAS Node Manager Shell")
public class CVShell {

	@Autowired
	private CVService cvService;

	@Autowired
	private CVNodeManager nodeManager;

	private ConsoleReader consoleReader;

	@ShellMethod("Show fingerprint of our TLS certificate")
	public void showCertFingerprint() throws Exception {
		X509Certificate cert = this.nodeManager.getConfig().getCert();
		String fingerprint = org.opentoolset.nettyagents.Utils.getFingerprintAsHex(cert);
		println(fingerprint);
	}

	@ShellMethod("Connect to an orchestrator")
	public void connect() throws Exception {
		String host = getInput("Enter orchestrator's IP or hostname (0 to exit):", input -> StringUtils.isNotBlank(input));
		if (host.equals("0")) {
			return;
		}

		Function<String, Integer> parser = (input) -> Integer.parseInt(input);
		String portStr = getInput("Enter orchestrator's port (0 to exit):", (input) -> Utils.noExcepion(() -> parser.apply(input)));
		if (portStr.equals("0")) {
			return;
		}

		int port = parser.apply(portStr);

		this.nodeManager.shutdown();
		Utils.waitFor(2);
		this.nodeManager.build();
		this.nodeManager.getConfig().setRemoteHost(host);
		this.nodeManager.getConfig().setRemotePort(port);
		this.cvService.addHandlers();
		this.nodeManager.startPeerIdentificationMode();
		this.nodeManager.startup();

		Utils.waitUntil(() -> this.nodeManager.getServer() != null, 10);
		PeerContext server = this.nodeManager.getServer();
		if (server == null) {
			println("The orchestrator couln't be connected. Please retry.");
			this.nodeManager.shutdown();
			return;
		}

		Utils.waitUntil(() -> this.nodeManager.getServer().getCert() != null, 10);
		X509Certificate orchestratorCert = server.getCert();
		if (orchestratorCert == null) {
			println("The orchestrator certificate couln't be gathered. Please switch the orchestrator to peer identification mode.");
			this.nodeManager.shutdown();
			return;
		}

		String fingerprint = org.opentoolset.nettyagents.Utils.getFingerprintAsHex(orchestratorCert);

		println("The orchestrator's fingerprint is: %s", fingerprint);

		String trustOrNot = getInput("Trust (Y) or not (N)?", input -> input.matches("[ynYN]"));
		switch (trustOrNot) {
			case "Y":
				this.nodeManager.setTrusted(server, fingerprint, orchestratorCert);
				this.nodeManager.stopPeerIdentificationMode();

				String orchestratorCertStr = org.opentoolset.nettyagents.Utils.base64Encode(orchestratorCert.getEncoded());
				CVConfig.setOrchestratorHost(host);
				CVConfig.setOrchestratorPort(port);
				CVConfig.setOrchestratorTLSCertificate(orchestratorCertStr);
				CVConfig.save();
				break;

			default:
				this.nodeManager.shutdown();
				break;
		}
	}

	@ShellMethod("Connect to an orchestrator")
	public void disconnect() throws Exception {

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