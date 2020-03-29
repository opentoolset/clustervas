package org.opentoolset.clustervas.demo;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.opentoolset.clustervas.demo.service.CVDemoService;
import org.opentoolset.clustervas.sdk.NodeManagerContext;
import org.opentoolset.nettyagents.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import jline.console.ConsoleReader;

@ShellComponent
public class CVDemoShell {

	@Autowired
	private CVDemoService service;

	@ShellMethod("List node managers waiting to be trusted")
	public void listNodeManagersWaitingToBeTrusted() throws Exception {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersWaiting();
		String result = nodeManagers.stream().map(nm -> buildNodeManagerStr(nm)).collect(Collectors.joining("\n"));
		print(result);
	}

	@ShellMethod("List trusted node managers")
	public void listNodeManagersTrusted() throws Exception {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersTrusted();
		String result = nodeManagers.stream().map(nm -> buildNodeManagerStr(nm)).collect(Collectors.joining("\n"));
		print(result);
	}

	@ShellMethod("Select and approve trust to a node manager")
	public void approveTrust() throws Exception {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersWaiting();
		String result = IntStream.range(1, nodeManagers.size() + 1).boxed().map(index -> String.format("%s - %s", index, buildNodeManagerStr(nodeManagers.get(index - 1)))).collect(Collectors.joining("\n"));
		if (StringUtils.isEmpty(result)) {
			print("There is no node manager waiting to be trusted");
			return;
		}

		print(result);
		selectAndPerform(nodeManagers, selection -> {
			NodeManagerContext nodeManager = nodeManagers.get(selection - 1);
			nodeManager.getPeerContext().setTrusted(true);
			print("Node manager is now trusted by you: %s", buildNodeManagerStr(nodeManager));
		});
	}

	@ShellMethod("Generate key-pair")
	public void revokeTrust() throws Exception {
		List<NodeManagerContext> nodeManagers = this.service.getNodeManagersTrusted();
		if (nodeManagers.isEmpty()) {
			print("There is no trusted node manager");
			return;
		}

		selectAndPerform(nodeManagers, selection -> {
			NodeManagerContext nodeManager = nodeManagers.get(selection - 1);
			nodeManager.getPeerContext().setTrusted(false);
			print("Node manager is now not trusted by you: %s", buildNodeManagerStr(nodeManager));
		});
	}

	// ------

	private void selectAndPerform(List<NodeManagerContext> nodeManagers, Consumer<Integer> performer) throws IOException {
		String nodeManagerListStr = IntStream.range(1, nodeManagers.size() + 1).boxed().map(index -> String.format("%s - %s", index, buildNodeManagerStr(nodeManagers.get(index - 1)))).collect(Collectors.joining("\n"));
		print(nodeManagerListStr);

		ConsoleReader consoleReader = new ConsoleReader();
		while (true) {
			String selectionStr = consoleReader.readLine("Select node manager number to revoke trust (0 to exit): ");
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
		byte[] fingerprint = Utils.getFingerprint(nodeManager.getPeerContext().getCert());
		String result = String.format("id: %s, fingerprint: %s", nodeManager.getPeerContext().getId(), fingerprint);
		return result;
	}

	private void print(String format, Object... args) {
		System.out.printf(format, args);
	}
}