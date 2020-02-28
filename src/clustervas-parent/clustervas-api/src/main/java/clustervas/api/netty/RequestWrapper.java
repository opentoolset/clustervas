// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import java.util.UUID;

public class RequestWrapper extends MessageWrapper<AbstractMessage> {

	private String id;

	public RequestWrapper(AbstractMessage message) {
		super(message);
		this.id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}
}
