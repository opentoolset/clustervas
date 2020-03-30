// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk;

import java.net.SocketAddress;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.opentoolset.clustervas.sdk.messages.NodeManagerInfoRequest;
import org.opentoolset.clustervas.sdk.messages.NodeManagerInfoResponse;
import org.opentoolset.clustervas.sdk.messages.cv.AbstractRequestFromNodeManager;
import org.opentoolset.nettyagents.AbstractMessage;
import org.opentoolset.nettyagents.AbstractRequest;
import org.opentoolset.nettyagents.Constants;
import org.opentoolset.nettyagents.PeerContext;
import org.opentoolset.nettyagents.Utils;
import org.opentoolset.nettyagents.agents.ServerAgent;
import org.opentoolset.nettyagents.agents.ServerAgent.Config;

public class CVAgent {

	private ServerAgent agent = new ServerAgent();

	private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private Map<SocketAddress, NodeManagerContext> nodeManagers = new HashMap<>();

	public CVAgent() {
		Config config = this.agent.getConfig();
		config.setTlsEnabled(true);
		config.setLocalPort(Constants.DEFAULT_SERVER_PORT);

		this.scheduledExecutor.scheduleWithFixedDelay(() -> maintenance(), 0, 1, TimeUnit.SECONDS);
	}

	public Config getConfig() {
		return this.agent.getConfig();
	}

	public void startPeerIdentificationMode() {
		this.agent.startPeerIdentificationMode();
	}

	public void stopPeerIdentificationMode() {
		this.agent.stopPeerIdentificationMode();
	}

	public boolean isInPeerIdentificationMode() {
		return this.agent.getContext().isTrustNegotiationMode();
	}

	public void startup() {
		this.agent.startup();
	}

	public void shutdown() {
		this.agent.shutdown();
	}

	public <TReq extends AbstractRequestFromNodeManager<TResp>, TResp extends AbstractMessage> void setRequestHandler(Class<TReq> classOfRequest, Function<TReq, TResp> function) {
		this.agent.setRequestHandler(classOfRequest, function);
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, NodeManagerContext nodeManager) {
		if (!nodeManager.getPeerContext().isTrusted()) {
			// TODO [mhdilrk] Throwing an exception may make more sense here
			return null;
		}

		return this.agent.doRequest(request, nodeManager.getPeerContext());
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, NodeManagerContext nodeManager, int timeoutSec) {
		if (!nodeManager.getPeerContext().isTrusted()) {
			// TODO [mhdilrk] Throwing an exception may make more sense here
			return null;
		}

		return this.agent.doRequest(request, nodeManager.getPeerContext(), timeoutSec);
	}

	public Map<SocketAddress, NodeManagerContext> getNodeManagers() {
		return nodeManagers;
	}

	public NodeManagerContext getNodeManager(String nodeManagerId) {
		if (StringUtils.isEmpty(nodeManagerId)) {
			return null;
		}

		for (NodeManagerContext nodeManager : this.nodeManagers.values()) {
			if (nodeManagerId.equals(nodeManager.getPeerContext().getId())) {
				return nodeManager;
			}
		}

		return null;
	}

	public void setTrusted(NodeManagerContext nodeManager, boolean trusted) {
		PeerContext peerContext = nodeManager.getPeerContext();
		peerContext.setTrusted(true);
		X509Certificate cert = peerContext.getCert();
		String fingerprint = Utils.getFingerprintAsHex(cert);
		this.agent.getContext().getTrustedCerts().put(fingerprint, cert);
	}

	// ---

	/**
	 * There should be no need to use this method. Suitable only for advanced usage.
	 * 
	 * @return
	 */
	protected ServerAgent getAgent() {
		return this.agent;
	}

	// ---

	private void maintenance() {
		synchronized (this.nodeManagers) {
			// TODO [hadidilek] Here, convert nodeManager maintenance method from polling to event handling
			Map<SocketAddress, PeerContext> clients = this.agent.getClients();
			for (SocketAddress socketAddress : clients.keySet()) {
				PeerContext peerContext = clients.get(socketAddress);
				this.nodeManagers.compute(socketAddress, (key, value) -> addOrUpdateNodeManagerContext(key, value, peerContext));
			}

			// TODO [hadidilek] Here, perform cleanup for old untrusted connections...
		}
	}

	private NodeManagerContext addOrUpdateNodeManagerContext(SocketAddress socketAddress, NodeManagerContext nodeManager, PeerContext peerContext) {
		nodeManager = nodeManager != null ? nodeManager : new NodeManagerContext();
		if (StringUtils.isEmpty(peerContext.getId()) && peerContext.isTrusted()) {
			try {
				NodeManagerInfoResponse response = doRequest(new NodeManagerInfoRequest(), nodeManager);
				if (response.isSuccessfull()) {
					peerContext.setId(response.getId());
				}
			} catch (Exception e) {
			}
		}
		nodeManager.setSocketAddress(socketAddress);
		nodeManager.setPeerContext(peerContext);
		return nodeManager;
	}
}
