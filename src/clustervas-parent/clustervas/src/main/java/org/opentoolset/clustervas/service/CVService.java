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
import org.opentoolset.clustervas.sdk.messages.LoadNewContainerRequest;
import org.opentoolset.clustervas.sdk.messages.LoadNewContainerResponse;
import org.opentoolset.clustervas.sdk.messages.NodeManagerInfoRequest;
import org.opentoolset.clustervas.sdk.messages.NodeManagerInfoResponse;
import org.opentoolset.clustervas.sdk.messages.RemoveContainerRequest;
import org.opentoolset.clustervas.sdk.messages.RemoveContainerResponse;
import org.opentoolset.clustervas.sdk.messages.SyncOperationRequest;
import org.opentoolset.clustervas.sdk.messages.SyncOperationRequest.Type;
import org.opentoolset.clustervas.sdk.messages.SyncOperationResponse;
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

	public void addHandlers() throws InvalidKeyException, CertificateException {
		this.nodeManager.setRequestHandler(NodeManagerInfoRequest.class, request -> handle(request));
		this.nodeManager.setRequestHandler(LoadNewContainerRequest.class, request -> handle(request));
		this.nodeManager.setRequestHandler(RemoveContainerRequest.class, request -> handle(request));
		this.nodeManager.setRequestHandler(GMPRequest.class, request -> handle(request));
		this.nodeManager.setRequestHandler(SyncOperationRequest.class, request -> handle(request));
	}

	public void reconnect() throws InvalidKeyException, CertificateException {
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

	// ---

	public NodeManagerInfoResponse handle(NodeManagerInfoRequest request) {
		NodeManagerInfoResponse response = new NodeManagerInfoResponse();
		response.setId(CVConfig.getId());
		response.setSuccessfull(true);
		return response;
	}

	public LoadNewContainerResponse handle(LoadNewContainerRequest request) {
		LoadNewContainerResponse response = new LoadNewContainerResponse();

		CVContainer cvContainer = this.containerService.loadNewContainer();
		if (cvContainer == null) {
			return response;
		}

		String nodeName = cvContainer.getName();

		if (!this.containerService.isContainerRunning(nodeName)) {
			this.containerService.removeContainer(nodeName);
			// TODO [hadi] inform user through response message about failure
			return response;
		}

		if (!ContainerUtils.waitUntilGvmdIsReady(nodeName, new Utils.TimeOutIndicator(120, TimeUnit.SECONDS))) {
			this.containerService.removeContainer(nodeName);
			// TODO [hadi] inform user through response message about failure
			return response;
		}

		response.setSuccessfull(true);
		response.setContainerName(nodeName);
		return response;
	}

	public RemoveContainerResponse handle(RemoveContainerRequest request) {
		RemoveContainerResponse response = new RemoveContainerResponse();

		boolean successfull = this.containerService.removeContainer(request.getContainerName());
		response.setSuccessfull(successfull);
		return response;
	}

	public GMPResponse handle(GMPRequest request) {
		GMPResponse gvmResponse = new GMPResponse();

		if (!this.containerService.isContainerRunning(request.getContainerName())) {
			// TODO [hadi] inform user through response message about illegal state
			return gvmResponse;
		}

		if (!ContainerUtils.waitUntilGvmdIsReady(request.getContainerName(), new Utils.TimeOutIndicator(10, TimeUnit.SECONDS))) {
			// TODO [hadi] inform user through response message about illegal state
			return gvmResponse;
		}

		CmdExecutor.Response response = ContainerUtils.dockerExec(request.getContainerName(), GVM_COMMAND, request.getXml());
		gvmResponse.setSuccessfull(response.isSuccessful());
		gvmResponse.setXml(response.getOutput());
		return gvmResponse;
	}

	public SyncOperationResponse handle(SyncOperationRequest request) {
		SyncOperationResponse response = new SyncOperationResponse();

		Type type = request.getType();
		if (type != null) {
			switch (type) {
				case DO_INTERNAL_SYNC: {
					boolean result = this.containerService.doInternalNVTSync();
					response.setSuccessfull(result);
					break;
				}

				case DO_POST_SYNC_OPERATIONS: {
					boolean result = this.containerService.loadOperationalImageFromTemplateContainer();
					response.setSuccessfull(result);
					break;
				}

				default:
					break;
			}

		}

		return response;
	}

	// ---

	@PostConstruct
	private void postConstruct() throws InvalidKeyException, CertificateException {
		reconnect();
	}

	@PreDestroy
	private void preDestroy() {
	}
}
