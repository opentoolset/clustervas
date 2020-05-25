// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.jline.utils.AttributedString;
import org.opentoolset.clustervas.service.CVNodeManager;
import org.opentoolset.clustervas.service.CVService;
import org.opentoolset.clustervas.utils.CVConfigProvider;
import org.opentoolset.clustervas.utils.Utils;
import org.opentoolset.nettyagents.PeerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import io.netty.channel.ChannelHandlerContext;
import jline.console.ConsoleReader;

@ShellComponent(value = "ClusterVAS Node Manager Shell")
public class CVShell {

	@Autowired
	private CVService cvService;

	@Autowired
	private CVNodeManager nodeManager;

	private ConsoleReader consoleReader;

	// ---

	@Bean
	private PromptProvider promptProvider() {
		return () -> new AttributedString("clustervas:> ");
	}

	// ---

	@ShellMethod("Show fingerprint of our TLS certificate")
	public void showCertFingerprint() throws Exception {
		X509Certificate cert = this.nodeManager.getConfig().getCert();
		String fingerprint = org.opentoolset.nettyagents.Utils.getFingerprintAsHex(cert);
		println(fingerprint);
	}

	@ShellMethod("Connect to an orchestrator")
	public void connect() throws Exception {
		String host = getInput("Enter orchestrator's IP or hostname (0 to exit): ", input -> StringUtils.isNotBlank(input));
		if (host.equals("0")) {
			return;
		}

		Function<String, Integer> parser = (input) -> Integer.parseInt(input);
		String portStr = getInput("Enter orchestrator's port (0 to exit): ", (input) -> Utils.noExcepion(() -> parser.apply(input)));
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

		PeerContext server = this.nodeManager.getServer();
		Utils.waitUntil(() -> server.getCert() != null, 10);
		X509Certificate orchestratorCert = server.getCert();
		if (orchestratorCert == null) {
			println("The orchestrator certificate couln't be gathered. Please switch the orchestrator to peer identification mode.");
			this.nodeManager.shutdown();
			connectionStatus();
			return;
		}

		String fingerprint = org.opentoolset.nettyagents.Utils.getFingerprintAsHex(orchestratorCert);

		println("The orchestrator's fingerprint is: %s", fingerprint);

		String trustOrNot = getInput("Trust (Y) or not (N)? ", input -> input.matches("[ynYN]"));
		switch (trustOrNot) {
			case "Y":
				this.nodeManager.setTrusted(server, fingerprint, orchestratorCert);
				this.nodeManager.stopPeerIdentificationMode();

				String orchestratorCertStr = org.opentoolset.nettyagents.Utils.base64Encode(orchestratorCert.getEncoded());
				CVConfig.setOrchestratorHost(host);
				CVConfig.setOrchestratorPort(port);
				CVConfig.setOrchestratorTLSCertificate(orchestratorCertStr);
				CVConfig.save();

				println("Connected");
				break;

			default:
				this.nodeManager.shutdown();
				break;
		}

		connectionStatus();
	}

	@ShellMethod("Reconnect to the configured orchestrator")
	public void reconnect() throws Exception {
		PeerContext server = this.nodeManager.getServer();
		
		this.nodeManager.shutdown();
		Utils.waitFor(2);
		println("Disconnected, reconnecting...");
		this.nodeManager.build();
		this.cvService.reconnect();
		Utils.waitUntil(() -> server.getChannelHandlerContext() != null, 10);
		println("Reconnected");
		connectionStatus();
	}

	@ShellMethod("Disconnect from the orchestrator (this preserves current configuration)")
	public void disconnect() throws Exception {
		PeerContext server = this.nodeManager.getServer();
		if (server.getChannelHandlerContext() != null) {
			this.nodeManager.shutdown();
			Utils.waitFor(2);
			println("Disconnected");
		}

		connectionStatus();
	}

	@ShellMethod("Show connection status")
	public void connectionStatus() throws Exception {
		PeerContext server = this.nodeManager.getServer();
		ChannelHandlerContext channelHandlerContext = server.getChannelHandlerContext();
		println("--- Connection status:");
		if (channelHandlerContext == null) {
			println("Not connected !");
		} else {
			println("Connection info: %s", channelHandlerContext.channel());
			println("Server trusted?: %s", server.isTrusted());
		}

		showConfig();
	}

	@ShellMethod("Show configuration")
	public void showConfig() throws Exception {
		println("--- Configuration:");
		Configuration config = CVConfigProvider.getConfig();
		Iterator<String> keys = config.getKeys();
		keys.forEachRemaining(key -> println("%s : %s", key, config.get(String.class, key)));
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