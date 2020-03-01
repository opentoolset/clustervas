// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty.agent;

import clustervas.api.netty.AbstractInboundMessageHandler;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.CVApiContext;
import clustervas.api.netty.MessageWrapper;

public class CVClientInboundMessageHandler extends AbstractInboundMessageHandler {

	private CVApiContext apiContext;

	// ---

	public CVClientInboundMessageHandler(CVApiContext apiContext) {
		this.apiContext = apiContext;
	}

	// ---

	@Override
	protected AbstractMessage processMessage(MessageWrapper requestWrapper) {
		// Not required yet
		return null;
	}

	@Override
	protected CVApiContext getApiContext() {
		return apiContext;
	}
}