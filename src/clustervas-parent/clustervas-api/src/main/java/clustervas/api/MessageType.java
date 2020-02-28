package clustervas.api;

import java.util.function.BiFunction;

import clustervas.api.MessageTypes.Type;
import clustervas.api.netty.AbstractMessage;

public class MessageType<T extends AbstractMessage> {

	private Class<T> messageClass;
	private Type type;
	private BiFunction<T, CVServerService, AbstractMessage> serverRequestProcessor;
	private BiFunction<T, CVClientService, AbstractMessage> clientRequestProcessor;

	public MessageType() {
	}

	public MessageType(Class<T> messageClass, Type type) {
		this();
		this.messageClass = messageClass;
		this.type = type;
	}

	public MessageType(Class<T> messageClass, Type type, BiFunction<T, CVServerService, AbstractMessage> serverRequestProcessor, BiFunction<T, CVClientService, AbstractMessage> clientRequestProcessor) {
		this(messageClass, type);
		this.serverRequestProcessor = serverRequestProcessor;
		this.clientRequestProcessor = clientRequestProcessor;
	}

	public Class<T> getMessageClass() {
		return messageClass;
	}

	public Type getType() {
		return type;
	}

	public BiFunction<T, CVServerService, AbstractMessage> getServerRequestProcessor() {
		return serverRequestProcessor;
	}

	public BiFunction<T, CVClientService, AbstractMessage> getClientRequestProcessor() {
		return clientRequestProcessor;
	}
}