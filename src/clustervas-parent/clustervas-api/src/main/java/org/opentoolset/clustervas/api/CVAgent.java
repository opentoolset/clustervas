package org.opentoolset.clustervas.api;

import java.util.function.Function;

import org.opentoolset.clustervas.api.messages.cv.AbstractRequestFromNodeManager;
import org.opentoolset.nettyagents.AbstractMessage;
import org.opentoolset.nettyagents.AbstractRequest;
import org.opentoolset.nettyagents.Constants;
import org.opentoolset.nettyagents.PeerContext;
import org.opentoolset.nettyagents.agents.ServerAgent;
import org.opentoolset.nettyagents.agents.ServerAgent.Config;

public class CVAgent {

	private ServerAgent agent = new ServerAgent();

	public CVAgent() {
		Config config = this.agent.getConfig();
		config.setTlsEnabled(true);
		config.setLocalPort(Constants.DEFAULT_SERVER_PORT);
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

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, PeerContext peerContext) {
		return this.agent.doRequest(request, peerContext);
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, PeerContext peerContext, int timeoutSec) {
		return this.agent.doRequest(request, peerContext, timeoutSec);
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
}
