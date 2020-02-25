// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api;

import clustervas.api.messages.SampleRequest;
import clustervas.api.messages.SampleResponse;

public interface MessageTypes {

	public class Tuple<T extends AbstractMessage> {

		private Class<T> messageClass;
		private Type type;

		public Tuple() {
		}

		public Tuple(Class<T> messageClass, Type type) {
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

	public enum Type {
		SAMPLE_REQUEST,
		SAMPLE_RESPONSE,
	}

	Tuple<SampleRequest> SAMPLE_REQUEST = new Tuple<>(SampleRequest.class, Type.SAMPLE_REQUEST);
	Tuple<SampleResponse> SAMPLE_RESPONSE = new Tuple<>(SampleResponse.class, Type.SAMPLE_RESPONSE);
}
