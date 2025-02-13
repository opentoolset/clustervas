// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk;

import java.net.SocketAddress;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CVAgent {

	private static Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	private ServerAgent agent = new ServerAgent();

	private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private Map<SocketAddress, NodeManagerContext> nodeManagers = new HashMap<>();

	private boolean started = false;

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
		synchronized (this.nodeManagers) {
			for (Entry<SocketAddress, NodeManagerContext> entry : new HashSet<>(this.nodeManagers.entrySet())) {
				NodeManagerContext nodeManagerContext = entry.getValue();
				if (!nodeManagerContext.getPeerContext().isTrusted()) {
					this.nodeManagers.remove(entry.getKey());
				}
			}
		}
	}

	public boolean isInPeerIdentificationMode() {
		return this.agent.getContext().isTrustNegotiationMode();
	}

	public void startup() {
		synchronized (this) {
			if (!this.started) {
				this.agent.startup();
				this.started = true;
			}
		}
	}

	public void shutdown() {
		synchronized (this) {
			if (this.started) {
				this.agent.shutdown();
				this.nodeManagers.clear();
				this.started = false;
			}
		}
	}

	public <TReq extends AbstractRequestFromNodeManager<TResp>, TResp extends AbstractMessage> void setRequestHandler(Class<TReq> classOfRequest, Function<TReq, TResp> function) {
		this.agent.setRequestHandler(classOfRequest, function);
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, NodeManagerContext nodeManager) {
		if (!nodeManager.getPeerContext().isTrusted()) {
			// TODO [hadidilek] Throwing an exception may make more sense here
			return null;
		}

		return this.agent.doRequest(request, nodeManager.getPeerContext());
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, NodeManagerContext nodeManager, int timeoutSec) {
		if (!nodeManager.getPeerContext().isTrusted()) {
			// TODO [hadidilek] Throwing an exception may make more sense here
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
		X509Certificate cert = peerContext.getCert();
		String fingerprint = Utils.getFingerprintAsHex(cert);

		peerContext.setTrusted(trusted);
		if (trusted) {
			this.agent.getContext().getTrustedCerts().put(fingerprint, cert);
		} else {
			this.agent.getContext().getTrustedCerts().remove(fingerprint);
		}

		// NodeManagerInfoRequest request = new NodeManagerInfoRequest();
		// NodeManagerInfoResponse doRequest = doRequest(request, nodeManager);
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
		try {
			synchronized (this.nodeManagers) {
				// TODO [hadidilek] Here, convert nodeManager maintenance method from polling to event handling
				Map<SocketAddress, PeerContext> clients = this.agent.getClients();

				for (SocketAddress nodeManagerSocketAddress : new ArrayList<>(this.nodeManagers.keySet())) {
					if (clients.keySet().stream().noneMatch(clientSocketAddress -> Objects.equals(clientSocketAddress, nodeManagerSocketAddress))) {
						this.nodeManagers.remove(nodeManagerSocketAddress);
					}
				}

				for (SocketAddress socketAddress : clients.keySet()) {
					PeerContext peerContext = clients.get(socketAddress);
					this.nodeManagers.compute(socketAddress, (key, value) -> addOrUpdateNodeManagerContext(key, value, peerContext));
				}
			}
		} catch (Exception e) {
			logger.debug(e.getLocalizedMessage(), e);
		}
	}

	private NodeManagerContext addOrUpdateNodeManagerContext(SocketAddress socketAddress, NodeManagerContext nodeManager, PeerContext peerContext) {
		nodeManager = nodeManager != null ? nodeManager : new NodeManagerContext();
		nodeManager.setSocketAddress(socketAddress);
		nodeManager.setPeerContext(peerContext);
		if (StringUtils.isEmpty(peerContext.getId()) && peerContext.isTrusted()) {
			try {
				NodeManagerInfoResponse response = doRequest(new NodeManagerInfoRequest(), nodeManager);
				if (response != null && response.isSuccessfull()) {
					peerContext.setId(response.getId());
				}
			} catch (Exception e) {
				logger.debug(e.getLocalizedMessage(), e);
			}
		}
		return nodeManager;
	}
}
