package org.opentoolset.clustervas.demo.service;

import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.opentoolset.clustervas.sdk.NodeManagerContext;
import org.opentoolset.clustervas.sdk.messages.GMPRequest;
import org.opentoolset.clustervas.sdk.messages.GMPResponse;
import org.opentoolset.clustervas.sdk.messages.LoadNewNodeRequest;
import org.opentoolset.clustervas.sdk.messages.LoadNewNodeResponse;
import org.opentoolset.clustervas.sdk.messages.RemoveNodeRequest;
import org.opentoolset.clustervas.sdk.messages.RemoveNodeResponse;
import org.opentoolset.clustervas.sdk.messages.cv.GetActiveNodesRequest;
import org.opentoolset.clustervas.sdk.messages.cv.GetActiveNodesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CVDemoOrchestratorService {

	@Autowired
	private CVDemoOrchestratorAgent agent;

	public List<NodeManagerContext> getNodeManagersWaiting() {
		return this.agent.getNodeManagers().values().stream().filter(nm -> !nm.getPeerContext().isTrusted()).collect(Collectors.toList());
	}

	public List<NodeManagerContext> getNodeManagersTrusted() {
		return this.agent.getNodeManagers().values().stream().filter(nm -> nm.getPeerContext().isTrusted()).collect(Collectors.toList());
	}

	public void startPeerIdentificationMode() {
		this.agent.startPeerIdentificationMode();
	}

	public void stopPeerIdentificationMode() {
		this.agent.stopPeerIdentificationMode();
	}

	public boolean isInPeerIdentificationMode() {
		return this.agent.isInPeerIdentificationMode();
	}

	public void setTrusted(NodeManagerContext nodeManager, boolean trusted) {
		this.agent.setTrusted(nodeManager, trusted);
	}

	public String sendLoadNewNodeRequest(NodeManagerContext nodeManager) {
		LoadNewNodeResponse response = this.agent.doRequest(new LoadNewNodeRequest(), nodeManager, 300);
		if (response != null && response.isSuccessfull()) {
			String newNodeName = response.getNodeName();
			nodeManager.getActiveNodes().add(newNodeName);
			return newNodeName;
		}

		return null;
	}

	public boolean sendRemoveNodeRequest(NodeManagerContext nodeManager, String nodeName) {
		RemoveNodeRequest request = new RemoveNodeRequest();
		request.setNodeName(nodeName);

		RemoveNodeResponse response = this.agent.doRequest(request, nodeManager, 60);
		if (response != null && response.isSuccessfull()) {
			nodeManager.getActiveNodes().remove(nodeName);
			return true;
		}

		return false;
	}

	public String sendGmpCommand(NodeManagerContext nodeManager, String nodeName, String commandXML) {
		GMPRequest request = new GMPRequest();
		request.setNodeName(nodeName);
		request.setXml(commandXML);

		GMPResponse response = this.agent.doRequest(request, nodeManager);
		return response.isSuccessfull() ? response.getXml() : null;
	}

	// ---

	@PostConstruct
	private void postContruct() throws CertificateException, InvalidKeyException {
		this.agent.setRequestHandler(GetActiveNodesRequest.class, request -> handle(request));
		this.agent.startup();
	}

	private GetActiveNodesResponse handle(GetActiveNodesRequest request) {
		GetActiveNodesResponse response = new GetActiveNodesResponse();

		String nodeManagerId = request.getSenderId();
		if (StringUtils.isEmpty(nodeManagerId)) {
			return response;
		}

		NodeManagerContext nodeManager = this.agent.getNodeManager(nodeManagerId);
		response.getNodeNames().addAll(nodeManager.getActiveNodes());
		response.setSuccessfull(true);
		return response;
	}
}
