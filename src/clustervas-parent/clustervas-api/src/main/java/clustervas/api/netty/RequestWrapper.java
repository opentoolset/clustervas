// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

public class RequestWrapper extends MessageWrapper {

	private String id;

	public RequestWrapper(AbstractMessage<?> message) {
		super(message);
	}

	public String getId() {
		return id;
	}
}
