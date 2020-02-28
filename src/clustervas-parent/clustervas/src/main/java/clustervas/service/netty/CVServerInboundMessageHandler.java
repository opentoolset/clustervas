// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import clustervas.api.CVServerService;
import clustervas.api.MessageType;
import clustervas.api.netty.AbstractInboundMessageHandler;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.MessageWrapper;
import clustervas.api.netty.RequestWrapper;
import clustervas.api.netty.ResponseWrapper;
import clustervas.utils.CVLogger;

@Component
public class CVServerInboundMessageHandler extends AbstractInboundMessageHandler {

	@Autowired
	private CVServerService serviceProvider;

	@Override
	protected ResponseWrapper processRequest(RequestWrapper requestWrapper) {
		AbstractMessage request = requestWrapper.deserializeMessage();

		MessageType<AbstractMessage> type = requestWrapper.getType();
		BiFunction<AbstractMessage, CVServerService, AbstractMessage> requestProcessor = type.getServerRequestProcessor();
		if (requestProcessor != null) {
			AbstractMessage response = requestProcessor.apply(request, serviceProvider);
			ResponseWrapper responseWrapper = new ResponseWrapper(response);
			return responseWrapper;
		} else {
			CVLogger.error("Response process hasn't been defined for type: {}", type);
		}

		return null;
	}

	@Override
	protected void processMessage(MessageWrapper<?> messageWrapper) {
		// Not requred yet
	}
}