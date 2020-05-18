// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class RemoveContainerRequest extends AbstractRequest<RemoveContainerResponse> {

	private String containerName;

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	@Override
	public Class<RemoveContainerResponse> getResponseClass() {
		return RemoveContainerResponse.class;
	}
}
