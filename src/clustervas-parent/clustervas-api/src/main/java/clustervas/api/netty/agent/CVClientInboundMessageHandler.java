// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty.agent;

import clustervas.api.CVClientService;
import clustervas.api.netty.AbstractInboundMessageHandler;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.MessageWrapper;

public class CVClientInboundMessageHandler extends AbstractInboundMessageHandler {

	private CVClientService clientServiceProvider;

	public void setClientServiceProvider(CVClientService clientServiceProvider) {
		this.clientServiceProvider = clientServiceProvider;
	}

	// ---

	@Override
	protected AbstractMessage processMessage(MessageWrapper requestWrapper) {
		if (this.clientServiceProvider == null) {
			return null;
		}

		// Not required yet
		return null;
	}
}