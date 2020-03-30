// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

public class NodeManagerInfoResponse extends AbstractResponse {

	private static final long serialVersionUID = 5196299320381072704L;

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
