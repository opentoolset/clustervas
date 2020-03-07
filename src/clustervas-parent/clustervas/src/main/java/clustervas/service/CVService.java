// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import clustervas.api.messages.GvmCliRequest;
import clustervas.api.messages.GvmCliResponse;
import clustervas.api.messages.LoadNewNodeRequest;
import clustervas.api.messages.LoadNewNodeResponse;
import clustervas.api.messages.RemoveNodeRequest;
import clustervas.api.messages.RemoveNodeResponse;
import clustervas.net.Constants;
import clustervas.service.ContainerService.CVContainer;
import clustervas.utils.CmdExecutor;
import clustervas.utils.Utils;

@Service
public class CVService extends AbstractService {

	private static String GVM_COMMAND_TEMPLATE = "gvm-cli socket --gmp-username %s --gmp-password %s";
	private static String GVM_COMMAND = String.format(GVM_COMMAND_TEMPLATE, Constants.DEFAULT_GVM_DEFAULT_USER, Constants.DEFAULT_GVM_DEFAULT_PASSWORD);

	@Autowired
	private ContainerService containerService;

	@Autowired
	private ContainerServiceLocalShell containerServiceLocalShell;

	@Autowired
	private CVAgent cvAgent;

	@PostConstruct
	private void postConstruct() {
		this.cvAgent.setRequestHandler(GvmCliRequest.class, request -> handle(request));
		this.cvAgent.setRequestHandler(LoadNewNodeRequest.class, request -> handle(request));
		this.cvAgent.setRequestHandler(RemoveNodeRequest.class, request -> handle(request));
	}

	@PreDestroy
	private void preDestroy() {
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

		if (!this.containerServiceLocalShell.waitUntilGvmdIsReady(nodeName, new Utils.TimeOutIndicator(120, TimeUnit.SECONDS))) {
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

	public GvmCliResponse handle(GvmCliRequest request) {
		GvmCliResponse gvmResponse = new GvmCliResponse();

		if (!this.containerService.isContainerRunning(request.getNodeName())) {
			// TODO [hadi] inform user through response message about illegal state
			return gvmResponse;
		}

		if (!this.containerServiceLocalShell.waitUntilGvmdIsReady(request.getNodeName(), new Utils.TimeOutIndicator(10, TimeUnit.SECONDS))) {
			// TODO [hadi] inform user through response message about illegal state
			return gvmResponse;
		}

		CmdExecutor.Response response = this.containerServiceLocalShell.dockerExec(request.getNodeName(), GVM_COMMAND, request.getXml());
		gvmResponse.setSuccessfull(response.isSuccessful());
		gvmResponse.setXml(response.getOutput());
		return gvmResponse;
	}
}
