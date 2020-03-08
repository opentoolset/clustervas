// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.messages;

import clustervas.net.AbstractRequest;

public class GetActiveNodesRequest extends AbstractRequest<GetActiveNodesResponse> {

	@Override
	public Class<GetActiveNodesResponse> getResponseClass() {
		return GetActiveNodesResponse.class;
	}
}
