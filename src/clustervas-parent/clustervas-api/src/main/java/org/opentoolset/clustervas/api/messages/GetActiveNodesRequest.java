// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.api.messages;

import org.opentoolset.clustervas.net.AbstractRequest;

public class GetActiveNodesRequest extends AbstractRequest<GetActiveNodesResponse> {

	@Override
	public Class<GetActiveNodesResponse> getResponseClass() {
		return GetActiveNodesResponse.class;
	}
}
