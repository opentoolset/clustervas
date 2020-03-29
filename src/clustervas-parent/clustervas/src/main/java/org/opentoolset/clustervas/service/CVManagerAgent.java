// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.service;

import java.util.function.Function;

import org.opentoolset.clustervas.api.messages.cv.AbstractRequestFromNodeManager;
import org.opentoolset.nettyagents.AbstractMessage;
import org.opentoolset.nettyagents.AbstractRequest;
import org.opentoolset.nettyagents.agents.ClientAgent;
import org.springframework.stereotype.Component;

@Component
public class CVManagerAgent {

	private ClientAgent agent;

	public <TReq extends AbstractRequestFromNodeManager<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request) {
		request.setSenderId("");
		return this.agent.doRequest(request);
	}

	public <TReq extends AbstractRequestFromNodeManager<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, int timeoutSec) {
		request.setSenderId("");
		return this.agent.doRequest(request, timeoutSec);
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> void setRequestHandler(Class<TReq> classOfRequest, Function<TReq, TResp> function) {
		this.agent.setRequestHandler(classOfRequest, function);
	}
}
