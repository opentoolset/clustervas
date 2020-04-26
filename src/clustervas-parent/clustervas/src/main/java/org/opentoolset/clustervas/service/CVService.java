// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.service;

import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.opentoolset.clustervas.CVConfig;
import org.opentoolset.clustervas.CVConstants;
import org.opentoolset.clustervas.sdk.messages.GMPRequest;
import org.opentoolset.clustervas.sdk.messages.GMPResponse;
import org.opentoolset.clustervas.sdk.messages.LoadNewNodeRequest;
import org.opentoolset.clustervas.sdk.messages.LoadNewNodeResponse;
import org.opentoolset.clustervas.sdk.messages.NodeManagerInfoRequest;
import org.opentoolset.clustervas.sdk.messages.NodeManagerInfoResponse;
import org.opentoolset.clustervas.sdk.messages.RemoveNodeRequest;
import org.opentoolset.clustervas.sdk.messages.RemoveNodeResponse;
import org.opentoolset.clustervas.service.ContainerService.CVContainer;
import org.opentoolset.clustervas.utils.CVLogger;
import org.opentoolset.clustervas.utils.CmdExecutor;
import org.opentoolset.clustervas.utils.ContainerUtils;
import org.opentoolset.clustervas.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CVService extends AbstractService {

	private static String GVM_COMMAND_TEMPLATE = "gvm-cli socket --gmp-username %s --gmp-password %s";
	private static String GVM_COMMAND = String.format(GVM_COMMAND_TEMPLATE, CVConstants.DEFAULT_GVM_DEFAULT_USER, CVConstants.DEFAULT_GVM_DEFAULT_PASSWORD);

	@Autowired
	private ContainerService containerService;

	@Autowired
	private CVNodeManager nodeManager;

	// ---

	public NodeManagerInfoResponse handle(NodeManagerInfoRequest request) {
		NodeManagerInfoResponse response = new NodeManagerInfoResponse();
		response.setId(CVConfig.getId());
		response.setSuccessfull(true);
		return response;
	}

	public LoadNewNodeResponse handle(LoadNewNodeRequest request) {
		LoadNewNodeResponse response = new LoadNewNodeResponse();

		CVContainer cvContainer = this.containerService.loadNewNodeContainer();
		if (cvContainer == null) {
			return response;
		}

		String nodeName = cvContainer.getName();

		if (!this.containerService.isContainerRunning(nodeName)) {
			this.containerService.removeNodeContainer(nodeName);
			// TODO [hadi] inform user through response message about failure
			return response;
		}

		if (!ContainerUtils.waitUntilGvmdIsReady(nodeName, new Utils.TimeOutIndicator(120, TimeUnit.SECONDS))) {
			this.containerService.removeNodeContainer(nodeName);
			// TODO [hadi] inform user through response message about failure
			return response;
		}

		response.setSuccessfull(true);
		response.setNodeName(nodeName);
		return response;
	}

	public RemoveNodeResponse handle(RemoveNodeRequest request) {
		RemoveNodeResponse response = new RemoveNodeResponse();

		boolean successfull = this.containerService.removeNodeContainer(request.getNodeName());
		response.setSuccessfull(successfull);
		return response;
	}

	public GMPResponse handle(GMPRequest request) {
		GMPResponse gvmResponse = new GMPResponse();

		if (!this.containerService.isContainerRunning(request.getNodeName())) {
			// TODO [hadi] inform user through response message about illegal state
			return gvmResponse;
		}

		if (!ContainerUtils.waitUntilGvmdIsReady(request.getNodeName(), new Utils.TimeOutIndicator(10, TimeUnit.SECONDS))) {
			// TODO [hadi] inform user through response message about illegal state
			return gvmResponse;
		}

		CmdExecutor.Response response = ContainerUtils.dockerExec(request.getNodeName(), GVM_COMMAND, request.getXml());
		gvmResponse.setSuccessfull(response.isSuccessful());
		gvmResponse.setXml(response.getOutput());
		return gvmResponse;
	}

	public void addHandlers() throws InvalidKeyException, CertificateException {
		this.nodeManager.setRequestHandler(NodeManagerInfoRequest.class, request -> handle(request));
		this.nodeManager.setRequestHandler(LoadNewNodeRequest.class, request -> handle(request));
		this.nodeManager.setRequestHandler(RemoveNodeRequest.class, request -> handle(request));
		this.nodeManager.setRequestHandler(GMPRequest.class, request -> handle(request));
	}

	// ---

	@PostConstruct
	private void postConstruct() throws InvalidKeyException, CertificateException {
		if (this.nodeManager == null) {
			CVLogger.info("Node manager couldn't be created");
			return;
		}

		if (!this.nodeManager.isConfigured()) {
			CVLogger.info("Node manager is not configured");
			return;
		}

		if (!this.nodeManager.hasTrustedOrchestrator()) {
			CVLogger.info("Node manager has no trusted orchestrator");
			return;
		}

		addHandlers();

		CVLogger.info("Node manager is starting...");
		if (this.nodeManager.startup()) {
			CVLogger.info("Node manager started");
		} else {
			CVLogger.info("Node manager didn't start");
		}
	}

	@PreDestroy
	private void preDestroy() {
	}
}
