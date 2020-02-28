package clustervas.api;

import java.io.Serializable;
import java.util.function.BiFunction;

import clustervas.api.MessageTypes.Type;
import clustervas.api.netty.AbstractMessage;

public class MessageType<T extends AbstractMessage<?>> implements Serializable {

	private static final long serialVersionUID = -8768461042162510516L;

	private Class<T> messageClass;
	private Type type;
	private BiFunction<T, CVService, AbstractMessage<?>> requestProcessor;

	public MessageType() {
	}

	public MessageType(Class<T> messageClass, Type type) {
		this();
		this.messageClass = messageClass;
		this.type = type;
	}

	public MessageType(Class<T> messageClass, Type type, BiFunction<T, CVService, AbstractMessage<?>> requestProcessor) {
		this(messageClass, type);
		this.requestProcessor = requestProcessor;
	}

	public Class<T> getMessageClass() {
		return messageClass;
	}

	public Type getType() {
		return type;
	}

	public BiFunction<T, CVService, AbstractMessage<?>> getRequestProcessor() {
		return requestProcessor;
	}
}