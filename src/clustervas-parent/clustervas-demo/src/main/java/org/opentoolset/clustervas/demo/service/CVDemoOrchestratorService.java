package org.opentoolset.clustervas.demo.service;

import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.opentoolset.clustervas.sdk.NodeManagerContext;
import org.opentoolset.clustervas.sdk.messages.GMPRequest;
import org.opentoolset.clustervas.sdk.messages.GMPResponse;
import org.opentoolset.clustervas.sdk.messages.LoadNewContainerRequest;
import org.opentoolset.clustervas.sdk.messages.LoadNewContainerResponse;
import org.opentoolset.clustervas.sdk.messages.RemoveContainerRequest;
import org.opentoolset.clustervas.sdk.messages.RemoveContainerResponse;
import org.opentoolset.clustervas.sdk.messages.SyncOperationRequest;
import org.opentoolset.clustervas.sdk.messages.SyncOperationRequest.Type;
import org.opentoolset.clustervas.sdk.messages.SyncOperationResponse;
import org.opentoolset.clustervas.sdk.messages.cv.GetManagedContainersRequest;
import org.opentoolset.clustervas.sdk.messages.cv.GetManagedContainersResponse;
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

	public String sendLoadNewContainerRequest(NodeManagerContext nodeManager) {
		LoadNewContainerResponse response = this.agent.doRequest(new LoadNewContainerRequest(), nodeManager, 300);
		if (response != null && response.isSuccessfull()) {
			String newContainerName = response.getContainerName();
			nodeManager.getManagedContainers().add(newContainerName);
			return newContainerName;
		}

		return null;
	}

	public boolean sendRemoveContainerRequest(NodeManagerContext nodeManager, String containerName) {
		RemoveContainerRequest request = new RemoveContainerRequest();
		request.setContainerName(containerName);

		RemoveContainerResponse response = this.agent.doRequest(request, nodeManager, 60);
		if (response != null && response.isSuccessfull()) {
			nodeManager.getManagedContainers().remove(containerName);
			return true;
		}

		return false;
	}

	public String sendGmpCommand(NodeManagerContext nodeManager, String containerName, String commandXML) {
		GMPRequest request = new GMPRequest();
		request.setContainerName(containerName);
		request.setXml(commandXML);

		GMPResponse response = this.agent.doRequest(request, nodeManager);
		return response.isSuccessfull() ? response.getXml() : null;
	}

	public boolean internalNvtSync(NodeManagerContext nodeManager) {
		SyncOperationRequest request = new SyncOperationRequest();
		request.setType(Type.DO_INTERNAL_SYNC);

		SyncOperationResponse response = this.agent.doRequest(request, nodeManager);
		return response.isSuccessfull();
	}

	public boolean doPostSyncOperations(NodeManagerContext nodeManager) {
		SyncOperationRequest request = new SyncOperationRequest();
		request.setType(Type.DO_POST_SYNC_OPERATIONS);

		SyncOperationResponse response = this.agent.doRequest(request, nodeManager);
		return response.isSuccessfull();
	}

	// ---

	@PostConstruct
	private void postContruct() throws CertificateException, InvalidKeyException {
		this.agent.setRequestHandler(GetManagedContainersRequest.class, request -> handle(request));
		this.agent.startup();
	}

	private GetManagedContainersResponse handle(GetManagedContainersRequest request) {
		GetManagedContainersResponse response = new GetManagedContainersResponse();

		String nodeManagerId = request.getSenderId();
		if (StringUtils.isEmpty(nodeManagerId)) {
			return response;
		}

		NodeManagerContext nodeManager = this.agent.getNodeManager(nodeManagerId);
		response.getContainerNames().addAll(nodeManager.getManagedContainers());
		response.setSuccessfull(true);
		return response;
	}
}
