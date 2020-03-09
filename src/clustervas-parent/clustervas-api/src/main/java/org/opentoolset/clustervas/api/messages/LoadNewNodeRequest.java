// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.api.messages;

import org.opentoolset.clustervas.net.AbstractRequest;

public class LoadNewNodeRequest extends AbstractRequest<LoadNewNodeResponse> {

	@Override
	public Class<LoadNewNodeResponse> getResponseClass() {
		return LoadNewNodeResponse.class;
	}
}
