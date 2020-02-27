// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api;

import clustervas.api.messages.SampleRequest;
import clustervas.api.messages.SampleResponse;

public interface MessageTypes {

	public enum Type {
		SAMPLE_REQUEST,
		SAMPLE_RESPONSE,
	}

	MessageType<SampleRequest> SAMPLE_REQUEST = new MessageType<>(SampleRequest.class, Type.SAMPLE_REQUEST);
	MessageType<SampleResponse> SAMPLE_RESPONSE = new MessageType<>(SampleResponse.class, Type.SAMPLE_RESPONSE);
}
