package clustervas.api.netty;

import clustervas.api.MessageTypes.Type;

public class MessageType<T extends AbstractMessage<?>> {

	private Class<T> messageClass;
	private Type type;

	public MessageType() {
	}

	public MessageType(Class<T> messageClass, Type type) {
		this();
		this.messageClass = messageClass;
		this.type = type;
	}

	public Class<T> getMessageClass() {
		return messageClass;
	}

	public Type getType() {
		return type;
	}
}