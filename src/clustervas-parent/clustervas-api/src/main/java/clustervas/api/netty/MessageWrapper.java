// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import clustervas.api.MessageType;

public class MessageWrapper {

	private MessageType<? extends AbstractMessage> type;
	private String serializedMessage;
	private Class<? extends AbstractMessage> classOfMessage;

	private String id;
	private String correlationId;

	// ---

	public MessageWrapper() {
	}

	public static <T extends AbstractMessage> MessageWrapper create(T message) {
		MessageWrapper messageWrapper = new MessageWrapper();
		messageWrapper.type = message.getType();
		messageWrapper.classOfMessage = message.getClass();
		messageWrapper.serializedMessage = messageWrapper.serialize(message);
		return messageWrapper;
	}

	public static <T extends AbstractMessage> MessageWrapper createRequest(T message) {
		MessageWrapper messageWrapper = create(message);
		messageWrapper.id = UUID.randomUUID().toString();
		return messageWrapper;
	}

	public static <T extends AbstractMessage> MessageWrapper createResponse(T message, String correlationId) {
		MessageWrapper messageWrapper = create(message);
		messageWrapper.correlationId = correlationId;
		return messageWrapper;
	}

	// --- Getters:

	public String getId() {
		return id;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public MessageType<? extends AbstractMessage> getType() {
		return type;
	}

	public String getSerializedMessage() {
		return serializedMessage;
	}

	// --- Helper methods:

	public String serialize() {
		return serialize(this);
	}

	public AbstractMessage deserializeMessage() {
		AbstractMessage message = deserialize(this.serializedMessage, classOfMessage);
		return message;
	}

	public <TMsg extends AbstractMessage> TMsg deserializeMessage(Class<TMsg> classOfMessage) {
		TMsg message = deserialize(this.serializedMessage, classOfMessage);
		return message;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	// ---

	private String serialize(Object obj) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			String serializedMessageWrapper = mapper.writeValueAsString(obj);
			return serializedMessageWrapper;
		} catch (JsonProcessingException e) {
			CVApiContext.getLogger().error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	// ---

	public static MessageWrapper deserialize(String serializedMessageWrapper) {
		MessageWrapper messageWrapper = deserialize(serializedMessageWrapper, MessageWrapper.class);
		return messageWrapper;
	}

	// ---

	private static <T> T deserialize(String serialized, Class<T> classOfObj) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			T message = mapper.readValue(serialized, classOfObj);
			return message;
		} catch (IOException e) {
			CVApiContext.getLogger().error(e.getLocalizedMessage(), e);
			return null;
		}
	}
}
