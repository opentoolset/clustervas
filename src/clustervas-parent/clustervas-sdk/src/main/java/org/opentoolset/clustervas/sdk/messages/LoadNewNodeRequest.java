// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class LoadNewNodeRequest extends AbstractRequest<LoadNewNodeResponse> {

	@Override
	public Class<LoadNewNodeResponse> getResponseClass() {
		return LoadNewNodeResponse.class;
	}
}
