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
public class CVDemoService {

	@Autowired
	private CVDemoAgent agent;

	public List<NodeManagerContext> getNodeManagersWaiting() {
		return this.agent.getNodeManagers().values().stream().filter(nm -> !nm.getPeerContext().isTrusted()).collect(Collectors.toList());
	}

	public List<NodeManagerContext> getNodeManagersTrusted() {
		return this.agent.getNodeManagers().values().stream().filter(nm -> nm.getPeerContext().isTrusted()).collect(Collectors.toList());
	}

	public void stopPeerIdentificationMode() {
		this.agent.stopPeerIdentificationMode();
	}

	public boolean isInPeerIdentificationMode() {
		return !this.agent.isInPeerIdentificationMode();
	}

	public String sendLoadNewNodeRequest(String nodeManagerId) {
		NodeManagerContext nodeManager = this.agent.getNodeManagers().get(nodeManagerId);
		LoadNewNodeResponse response = this.agent.doRequest(new LoadNewNodeRequest(), nodeManager);
		if (response != null && response.isSuccessfull()) {
			String newNodeName = response.getNodeName();
			nodeManager.getActiveNodes().add(newNodeName);
			return newNodeName;
		}

		return null;
	}

	public boolean sendRemoveNodeRequest(String nodeManagerId, String nodeName) {
		RemoveNodeRequest request = new RemoveNodeRequest();
		request.setNodeName(nodeName);

		NodeManagerContext nodeManager = this.agent.getNodeManagers().get(nodeManagerId);
		RemoveNodeResponse response = this.agent.doRequest(request, nodeManager);
		if (response != null && response.isSuccessfull()) {
			nodeManager.getActiveNodes().remove(nodeName);
			return true;
		}

		return false;
	}

	public String sendGMPCommand(String nodeManagerId, String nodeName, String commandXML) {
		GMPRequest request = new GMPRequest();
		request.setNodeName(nodeName);
		request.setXml(commandXML);

		NodeManagerContext nodeManager = this.agent.getNodeManagers().get(nodeManagerId);
		GMPResponse response = this.agent.doRequest(request, nodeManager);
		return response.isSuccessfull() ? response.getXml() : null;
	}

	// ---

	@PostConstruct
	private void postContruct() throws CertificateException, InvalidKeyException {
		this.agent.setRequestHandler(GetActiveNodesRequest.class, request -> handle(request));
		this.agent.startPeerIdentificationMode();
		this.agent.startup();
	}

	private GetActiveNodesResponse handle(GetActiveNodesRequest request) {
		GetActiveNodesResponse response = new GetActiveNodesResponse();

		String nodeManagerId = request.getSenderId();
		if (StringUtils.isEmpty(nodeManagerId)) {
			return response;
		}

		NodeManagerContext nodeManager = this.agent.getNodeManagers().get(nodeManagerId);
		response.getNodeNames().addAll(nodeManager.getActiveNodes());
		return response;
	}
}
