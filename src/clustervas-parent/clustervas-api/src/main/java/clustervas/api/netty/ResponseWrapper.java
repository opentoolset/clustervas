// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

public class ResponseWrapper extends MessageWrapper {

	private String requestId;

	public ResponseWrapper(AbstractMessage<?> message) {
		super(message);
	}

	public String getRequestId() {
		return requestId;
	}
}
