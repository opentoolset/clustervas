// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api;

import java.io.IOException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import clustervas.api.MessageTypes.Tuple;

public class MessageWrapper {

	private Tuple<? extends AbstractMessage> type;

	private String serializedMessage;

	// ---

	public MessageWrapper() {
	}

	public <T extends AbstractMessage> MessageWrapper(T message, Tuple<T> type) {
		this();
		setMessage(message, type);
	}

	// --- Getters:

	public Tuple<? extends AbstractMessage> getType() {
		return type;
	}

	public String getSerializedMessage() {
		return serializedMessage;
	}

	// --- Helper methods:

	public String serialize() {
		return serialize(this);
	}

	public <T extends AbstractMessage> T getMessage(Class<T> classOfMessage) {
		return deserialize(this.serializedMessage, classOfMessage);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	// ---

	private <T extends AbstractMessage> void setMessage(T message, Tuple<T> type) {
		this.serializedMessage = serialize(message);
		this.type = type;
	}

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

	public static MessageWrapper deserialize(String serializedMessageWrapper) {
		return deserialize(serializedMessageWrapper, MessageWrapper.class);
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
