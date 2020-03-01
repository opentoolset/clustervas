// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;

public class MessageReceiver {

	private static Logger logger = Context.getLogger();

	private Map<Class<? extends AbstractMessage>, Function<AbstractRequest<?>, AbstractMessage>> requestHandlers = new HashMap<>();

	private Map<Class<? extends AbstractMessage>, Consumer<AbstractMessage>> messageHandlers = new HashMap<>();

	// ---

	@SuppressWarnings("unchecked")
	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> void setRequestHandler(Class<TReq> classOfRequest, Function<TReq, TResp> function) {
		requestHandlers.put(classOfRequest, (Function<AbstractRequest<?>, AbstractMessage>) function);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractMessage> void setMessageHandler(Class<T> classOfMessage, Consumer<T> consumer) {
		messageHandlers.put(classOfMessage, (Consumer<AbstractMessage>) consumer);
	}

	// ---

	AbstractMessage handleRequest(MessageWrapper messageWrapper) {
		AbstractMessage message = messageWrapper.deserializeMessage();
		if (message == null) {
			logger.warn("Request message is null");
			return null;
		}

		if (!AbstractRequest.class.isInstance(message)) {
			logger.warn("Request message is not a request. It's class: {}", message.getClass());
			return null;
		}

		AbstractRequest<?> request = (AbstractRequest<?>) message;
		Function<AbstractRequest<?>, AbstractMessage> function = requestHandlers.get(request.getClass());
		if (function == null) {
			logger.warn("Unsupported operation for request class: {}", request.getClass());
			return null;
		}

		AbstractMessage response = function.apply(request);
		return response;
	}

	void handleMessage(MessageWrapper messageWrapper) {
		AbstractMessage message = messageWrapper.deserializeMessage();
		if (message == null) {
			logger.warn("Request message is null");
			return;
		}

		Consumer<AbstractMessage> consumer = messageHandlers.get(message.getClass());
		if (consumer == null) {
			logger.warn("Unsupported operation for message class: {}", message.getClass());
			return;
		}

		consumer.accept(message);
	}
}
