package org.opentoolset.clustervas.demo.service;

import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.opentoolset.clustervas.api.messages.GMPRequest;
import org.opentoolset.clustervas.api.messages.GMPResponse;
import org.opentoolset.clustervas.api.messages.LoadNewNodeRequest;
import org.opentoolset.clustervas.api.messages.LoadNewNodeResponse;
import org.opentoolset.clustervas.api.messages.RemoveNodeRequest;
import org.opentoolset.clustervas.api.messages.RemoveNodeResponse;
import org.opentoolset.clustervas.api.messages.cv.GetActiveNodesRequest;
import org.opentoolset.clustervas.api.messages.cv.GetActiveNodesResponse;
import org.opentoolset.nettyagents.PeerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CVDemoService {

	@Autowired
	private CVDemoAgent agent;

	private List<PeerContext> nodeManagersWaiting = new ArrayList<>();
	private List<PeerContext> nodeManagersTrusted = new ArrayList<>();
	private Map<String, PeerContext> connectedNodeManagers = new HashMap<>();
	private Map<String, List<String>> activeNodesGroupedByNodeManagers = new HashMap<>();

	public List<PeerContext> getNodeManagersWaiting() {
		return this.nodeManagersWaiting;
	}

	public List<PeerContext> getNodeManagersTrusted() {
		return this.nodeManagersTrusted;
	}

	public void stopPeerIdentificationMode() {
		this.agent.stopPeerIdentificationMode();
	}

	public boolean isInPeerIdentificationMode() {
		return !this.agent.isInPeerIdentificationMode();
	}

	public String sendLoadNewNodeRequest(String nodeManagerId) {
		PeerContext nodeManager = this.connectedNodeManagers.get(nodeManagerId);
		LoadNewNodeResponse response = this.agent.doRequest(new LoadNewNodeRequest(), nodeManager);
		if (response.isSuccessfull()) {
			List<String> activeNodes = this.activeNodesGroupedByNodeManagers.get(nodeManagerId);
			String newNodeName = response.getNodeName();
			activeNodes.add(newNodeName);
			return newNodeName;
		}

		return null;
	}

	public boolean handle(String nodeManagerId, String nodeName) {
		RemoveNodeRequest request = new RemoveNodeRequest();
		request.setNodeName(nodeName);

		PeerContext nodeManager = this.connectedNodeManagers.get(nodeManagerId);
		RemoveNodeResponse response = this.agent.doRequest(request, nodeManager);
		return response.isSuccessfull();
	}

	public String sendGMPCommand(String nodeManagerId, String nodeName, String commandXML) {
		GMPRequest request = new GMPRequest();
		request.setNodeName(nodeName);
		request.setXml(commandXML);

		PeerContext nodeManager = this.connectedNodeManagers.get(nodeManagerId);
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

		String senderId = request.getSenderId();
		if (StringUtils.isEmpty(senderId)) {
			return response;
		}

		List<String> activeNodes = this.activeNodesGroupedByNodeManagers.get(senderId);
		response.getNodeNames().addAll(activeNodes);
		return response;
	}
}
