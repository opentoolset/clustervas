// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.api.messages;

import org.opentoolset.clustervas.net.AbstractRequest;

public class RemoveNodeRequest extends AbstractRequest<RemoveNodeResponse> {

	private String nodeName;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@Override
	public Class<RemoveNodeResponse> getResponseClass() {
		return RemoveNodeResponse.class;
	}
}
