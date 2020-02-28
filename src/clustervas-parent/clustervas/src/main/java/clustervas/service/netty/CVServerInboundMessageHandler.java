// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import clustervas.api.CVServerService;
import clustervas.api.MessageTypes;
import clustervas.api.netty.AbstractInboundMessageHandler;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.MessageWrapper;

@Component
public class CVServerInboundMessageHandler extends AbstractInboundMessageHandler {

	@Autowired
	private CVServerService serviceProvider;

	@Override
	protected AbstractMessage processMessage(MessageWrapper messageWrapper) {
		AbstractMessage response = null;

		switch (messageWrapper.getType().getType()) {
			case SAMPLE_REQUEST: {
				response = serviceProvider.getSampleResponse(messageWrapper.deserializeMessage(MessageTypes.SAMPLE_REQUEST.getMessageClass()));
				break;
			}

			default:
				break;
		}

		return response;
	}
}