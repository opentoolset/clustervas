// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty.agent;

import java.util.function.BiFunction;

import clustervas.api.CVClientService;
import clustervas.api.MessageType;
import clustervas.api.netty.AbstractInboundMessageHandler;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.CVApiContext;
import clustervas.api.netty.MessageWrapper;
import clustervas.api.netty.RequestWrapper;
import clustervas.api.netty.ResponseWrapper;

public class CVClientInboundMessageHandler extends AbstractInboundMessageHandler {

	private CVClientService clientServiceProvider;

	public void setClientServiceProvider(CVClientService clientServiceProvider) {
		this.clientServiceProvider = clientServiceProvider;
	}

	// ---

	@Override
	protected ResponseWrapper processRequest(RequestWrapper requestWrapper) {
		if (this.clientServiceProvider == null) {
			return null;
		}

		AbstractMessage request = requestWrapper.deserializeMessage();

		MessageType<AbstractMessage> type = requestWrapper.getType();
		BiFunction<AbstractMessage, CVClientService, AbstractMessage> requestProcessor = type.getClientRequestProcessor();
		if (requestProcessor != null) {
			AbstractMessage response = requestProcessor.apply(request, clientServiceProvider);
			ResponseWrapper responseWrapper = new ResponseWrapper(response);
			return responseWrapper;
		} else {
			CVApiContext.getLogger().error("Response process hasn't been defined for type: {}", type);
		}

		return null;
	}

	@Override
	protected void processMessage(MessageWrapper<?> messageWrapper) {
		// Not requred yet
	}
}