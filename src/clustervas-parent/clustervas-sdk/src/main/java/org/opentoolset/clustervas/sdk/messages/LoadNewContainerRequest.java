// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class LoadNewContainerRequest extends AbstractRequest<LoadNewContainerResponse> {

	@Override
	public Class<LoadNewContainerResponse> getResponseClass() {
		return LoadNewContainerResponse.class;
	}
}
