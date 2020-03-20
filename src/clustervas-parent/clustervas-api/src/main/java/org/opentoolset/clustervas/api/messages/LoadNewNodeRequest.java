// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.api.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class LoadNewNodeRequest extends AbstractRequest<LoadNewNodeResponse> {

	private static final long serialVersionUID = 6472084623796768555L;

	@Override
	public Class<LoadNewNodeResponse> getResponseClass() {
		return LoadNewNodeResponse.class;
	}
}
