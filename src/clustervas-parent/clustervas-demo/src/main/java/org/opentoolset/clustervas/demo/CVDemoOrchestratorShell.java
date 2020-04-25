package org.opentoolset.clustervas.demo;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.opentoolset.clustervas.demo.service.CVDemoService;
import org.opentoolset.clustervas.sdk.CVAgent;
import org.opentoolset.clustervas.sdk.NodeManagerContext;
import org.opentoolset.nettyagents.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import jline.console.ConsoleReader;

@ShellComponent
public class CVDemoOrchestratorShell {

	@Autowired
	private CVDemoService service;

	@Autowired
	private CVAgent agent;

	private ConsoleReader consoleReader;

	@ShellMethod("Show fingerprint of our TLS certificate")
	public void showCertFingerprint() throws Exception {
		X509Certificate cert = agent.getConfig().getCert();
		String fingerprint = Utils.getFingerprintAsHex(cert);
		println(fingerprint);
	}
	
	@ShellMethod("List node managers waiting to be trusted")
	public void listNodeManagersWaitingToBeTrusted() throws Exception {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersWaiting();
		String result = nodeManagers.stream().map(nm -> buildNodeManagerStr(nm)).collect(Collectors.joining("\n"));
		println(result);
	}

	@ShellMethod("List trusted node managers")
	public void listNodeManagersTrusted() throws Exception {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersTrusted();
		String result = nodeManagers.stream().map(nm -> buildNodeManagerStr(nm)).collect(Collectors.joining("\n"));
		println(result);
	}

	@ShellMethod("Select and approve trust to a node manager")
	public void approveTrust() throws Exception {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersWaiting();
		String result = IntStream.range(1, nodeManagers.size() + 1).boxed().map(index -> String.format("%s - %s", index, buildNodeManagerStr(nodeManagers.get(index - 1)))).collect(Collectors.joining("\n"));
		if (StringUtils.isEmpty(result)) {
			println("There is no node manager waiting to be trusted");
			return;
		}

		selectAndPerform(nodeManagers, selection -> {
			NodeManagerContext nodeManager = nodeManagers.get(selection - 1);
			this.service.setTrusted(nodeManager, true);
			println("Node manager is now trusted by you: %s", buildNodeManagerStr(nodeManager));
		});
	}

	@ShellMethod("Select and revoke trust to a node manager")
	public void revokeTrust() throws Exception {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersTrusted();
		if (nodeManagers.isEmpty()) {
			println("There is no trusted node manager");
			return;
		}

		selectAndPerform(nodeManagers, selection -> {
			NodeManagerContext nodeManager = nodeManagers.get(selection - 1);
			this.service.setTrusted(nodeManager, false);
			println("Node manager is now not trusted by you: %s", buildNodeManagerStr(nodeManager));
		});
	}

	@ShellMethod("Start peer identification mode")
	public void startPeerIdentificationMode() {
		this.service.startPeerIdentificationMode();
		println("started");
	}

	@ShellMethod("Stop peer identification mode")
	public void stopPeerIdentificationMode() {
		this.service.stopPeerIdentificationMode();
		println("stopped");
	}

	@ShellMethod("Load new node")
	public void loadNewNode() throws IOException {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersTrusted();
		if (nodeManagers.isEmpty()) {
			println("There is no trusted node manager");
			return;
		}

		selectAndPerform(nodeManagers, selection -> {
			NodeManagerContext nodeManager = nodeManagers.get(selection - 1);
			String sendLoadNewNodeRequest = this.service.sendLoadNewNodeRequest(nodeManager);
			println("New node name: %s", sendLoadNewNodeRequest);
		});
	}

	@ShellMethod("Load new node")
	public void removeNode() throws IOException {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersTrusted();
		if (nodeManagers.isEmpty()) {
			println("There is no trusted node manager");
			return;
		}

		selectAndPerform(nodeManagers, selection -> {
			NodeManagerContext nodeManager = nodeManagers.get(selection - 1);

			String result = IntStream.range(1, nodeManagers.size() + 1).boxed().map(index -> String.format("%s - %s", index, buildNodeManagerStr(nodeManagers.get(index - 1)))).collect(Collectors.joining("\n"));

			nodeManager.getActiveNodes();

			try {
				String nodeName = consoleReader.readLine("Enter node name to revoke: ");
				boolean removed = this.service.sendRemoveNodeRequest(nodeManager, nodeName);
				println("Removed: %s", removed);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	// ------

	@PostConstruct
	private void postConstruct() throws IOException {
		this.consoleReader = new ConsoleReader();
	}

	private void selectAndPerform(List<NodeManagerContext> nodeManagers, Consumer<Integer> performer) throws IOException {
		String nodeManagerListStr = IntStream.range(1, nodeManagers.size() + 1).boxed().map(index -> String.format("%s - %s", index, buildNodeManagerStr(nodeManagers.get(index - 1)))).collect(Collectors.joining("\n"));
		println(nodeManagerListStr);

		while (true) {
			String selectionStr = this.consoleReader.readLine("Select node manager number to revoke trust (0 to exit): ");
			try {
				int selection = Integer.parseInt(selectionStr);
				if (selection == 0) {
					return;
				}

				if (selection > 0 || selection <= nodeManagers.size()) {
					performer.accept(selection);
					return;
				}
			} catch (Exception e) {
			}
		}
	}

	private String buildNodeManagerStr(NodeManagerContext nodeManager) {
		String result = String.format("id: %s, fingerprint: %s", nodeManager.getPeerContext().getId(), Utils.getFingerprintAsHex(nodeManager.getPeerContext().getCert()));
		return result;
	}

	private void println(String format, Object... args) {
		print(format + "\n", args);
	}

	private void print(String format, Object... args) {
		System.out.printf(format, args);
	}
}