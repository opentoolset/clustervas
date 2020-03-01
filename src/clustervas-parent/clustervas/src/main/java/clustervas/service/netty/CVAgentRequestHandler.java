// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import clustervas.api.netty.AbstractInboundMessageHandler;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.CVApiContext;
import clustervas.api.netty.MessageWrapper;
import clustervas.utils.CVLogger;

@Component
public class CVAgentRequestHandler extends AbstractInboundMessageHandler {

	@Autowired
	private CVServerApiContext context;

	private Map<Class<? extends AbstractMessage>, Function<AbstractMessage, AbstractMessage>> operationHandlers = new HashMap<>();

	// ---

	@Override
	protected CVApiContext getApiContext() {
		return context;
	}

	@Override
	protected AbstractMessage processMessage(MessageWrapper messageWrapper) {
		AbstractMessage message = messageWrapper.deserializeMessage();
		if (message == null) {
			CVLogger.warn("Request message is null");
			return null;
		}

		Function<AbstractMessage, AbstractMessage> function = operationHandlers.get(message.getClass());
		if (function == null) {
			CVLogger.warn("Unsupported operation for request class: {}", message.getClass());
			return null;
		}

		AbstractMessage response = function.apply((AbstractMessage) message);
		return response;
	}

	@SuppressWarnings("unchecked")
	public <TReq extends AbstractMessage, TResp extends AbstractMessage> void setHandler(Class<TReq> classOfRequest, Function<TReq, TResp> function) {
		operationHandlers.put(classOfRequest, (Function<AbstractMessage, AbstractMessage>) function);
	}
}