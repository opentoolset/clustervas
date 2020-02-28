// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import java.io.IOException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import clustervas.api.MessageType;

public class MessageWrapper<T extends AbstractMessage<?>> {

	private MessageType<T> type;
	private String serializedMessage;

	@SuppressWarnings("rawtypes")
	private Class<? extends AbstractMessage> classOfMessage;

	// ---

	public MessageWrapper() {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MessageWrapper(AbstractMessage message) {
		this();
		this.type = message.getType();
		this.classOfMessage = message.getClass();
		this.serializedMessage = serialize(message);
	}

	// --- Getters:

	public MessageType<T> getType() {
		return type;
	}

	public String getSerializedMessage() {
		return serializedMessage;
	}

	// --- Helper methods:

	public String serialize() {
		return serialize(this);
	}

	public AbstractMessage<?> getMessage() {
		AbstractMessage<?> message = deserialize(this.serializedMessage, classOfMessage);
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
			e.printStackTrace();
			return null;
		}
	}

	// ---

	public static MessageWrapper<? extends AbstractMessage<?>> deserialize(String serializedMessageWrapper) {
		@SuppressWarnings("unchecked")
		MessageWrapper<AbstractMessage<?>> messageWrapper = deserialize(serializedMessageWrapper, MessageWrapper.class);
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
			e.printStackTrace();
			return null;
		}
	}
}
