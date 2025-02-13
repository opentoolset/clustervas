package org.opentoolset.clustervas.demo;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.opentoolset.clustervas.demo.service.CVDemoOrchestratorService;
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
	private CVDemoOrchestratorService service;

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
		if (nodeManagers.isEmpty()) {
			println("There is no node manager waiting for trust");
			return;
		}

		Integer index = select("Select node manager to approve trust", nodeManagers.stream().map(nodeManager -> buildNodeManagerStr(nodeManager)).collect(Collectors.toList()));
		if (index == null) {
			return;
		}

		NodeManagerContext nodeManager = nodeManagers.get(index);
		if (nodeManager == null) {
			return;
		}

		this.service.setTrusted(nodeManager, true);
		println("Node manager is now trusted by you: %s", buildNodeManagerStr(nodeManager));
	}

	@ShellMethod("Select and revoke trust to a node manager")
	public void revokeTrust() throws Exception {
		NodeManagerContext nodeManager = selectNodeManager("Select node manager to revoke trust");
		if (nodeManager == null) {
			return;
		}

		this.service.setTrusted(nodeManager, false);
		println("Node manager is now not trusted by you: %s", buildNodeManagerStr(nodeManager));
	}

	@ShellMethod("Show mode")
	public void showMode() {
		println(this.service.isInPeerIdentificationMode() ? "peer idendification mode" : "normal mode");
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

	@ShellMethod("Load new container")
	public void loadNewContainer() throws IOException {
		NodeManagerContext nodeManager = selectNodeManager("Select node manager to load a new container");
		if (nodeManager == null) {
			return;
		}

		String response = this.service.sendLoadNewContainerRequest(nodeManager);
		println("New container name: %s", response);
	}

	@ShellMethod("Remove container")
	public void removeContainer() throws IOException {
		NodeManagerContext nodeManager = selectNodeManager("Select node manager to remove a container");
		if (nodeManager == null) {
			return;
		}

		println("Selected node manager: %s", nodeManager.getPeerContext().getId());

		List<String> managedContainers = nodeManager.getManagedContainers();
		if (managedContainers.isEmpty()) {
			println("There is no managed container");
			return;
		}

		Integer index = select("Select a container to remove", managedContainers);
		if (index == null) {
			return;
		}

		String selectedContainerName = managedContainers.get(index);
		if (selectedContainerName == null) {
			return;
		}

		boolean result = this.service.sendRemoveContainerRequest(nodeManager, selectedContainerName);
		println("Removed: %s", result);
	}

	@ShellMethod("Send GMP command")
	public void sendGmpCommand() throws IOException {
		NodeManagerContext nodeManager = selectNodeManager("Select node manager to send a GMP command");
		if (nodeManager == null) {
			return;
		}

		println("Selected node manager: %s", nodeManager.getPeerContext().getId());

		List<String> managedContainers = nodeManager.getManagedContainers();
		if (managedContainers.isEmpty()) {
			println("There is no managed container");
			return;
		}

		Integer index = select("Select a container to send a GMP command", managedContainers);
		if (index == null) {
			return;
		}

		String selectedContainerName = managedContainers.get(index);
		if (selectedContainerName == null) {
			return;
		}

		String commandXML = getInput("GMP command as XML (0 to exit): ", input -> StringUtils.isNotBlank(input)).trim();
		String gmpResponse = this.service.sendGmpCommand(nodeManager, selectedContainerName, commandXML);
		println("GMP response:");
		println(gmpResponse);
	}

	@ShellMethod("Perform internal NVT synchronization")
	public void internalNvtSync() throws IOException {
		NodeManagerContext nodeManager = selectNodeManager("Select node manager to start internal NVT synchronization");
		if (nodeManager == null) {
			return;
		}

		println("Selected node manager: %s", nodeManager.getPeerContext().getId());

		boolean result = this.service.internalNvtSync(nodeManager);
		println(result ? "Succeeded" : "Failed");
	}

	@ShellMethod("Perform post sync operations")
	public void doPostSyncOperations() throws IOException {
		NodeManagerContext nodeManager = selectNodeManager("Select node manager to perform post synchronization operations");
		if (nodeManager == null) {
			return;
		}

		println("Selected node manager: %s", nodeManager.getPeerContext().getId());

		boolean result = this.service.doPostSyncOperations(nodeManager);
		println(result ? "Succeeded" : "Failed");
	}

	// ------

	@PostConstruct
	private void postConstruct() throws IOException {
		this.consoleReader = new ConsoleReader();
	}

	private NodeManagerContext selectNodeManager(String prompt) throws IOException {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersTrusted();
		if (nodeManagers.isEmpty()) {
			println("There is no trusted node manager");
			return null;
		}

		Integer index = select(prompt, nodeManagers.stream().map(nodeManager -> buildNodeManagerStr(nodeManager)).collect(Collectors.toList()));
		if (index == null) {
			return null;
		}

		NodeManagerContext nodeManager = nodeManagers.get(index);
		return nodeManager;
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

	private Integer select(String prompt, List<String> selectables) throws IOException {
		String listStr = IntStream.range(1, selectables.size() + 1).boxed().map(index -> String.format("%s - %s", index, selectables.get(index - 1))).collect(Collectors.joining("\n"));
		println(prompt);
		println(listStr);

		while (true) {
			String selectionStr = this.consoleReader.readLine("Select one option (0 to exit): ");
			try {
				int selectionIndex = Integer.parseInt(selectionStr);
				if (selectionIndex == 0) {
					return null;
				}

				return selectionIndex - 1;
			} catch (Exception e) {
			}
		}
	}

	private String buildNodeManagerStr(NodeManagerContext nodeManager) {
		String result = String.format("id: %s, fingerprint: %s, socket: %s", nodeManager.getPeerContext().getId(), Utils.getFingerprintAsHex(nodeManager.getPeerContext().getCert()), nodeManager.getSocketAddress());
		return result;
	}

	private void println(String format, Object... args) {
		print(format + "\n", args);
	}

	private void print(String format, Object... args) {
		System.out.printf(format, args);
	}
}