// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.api.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class GetActiveNodesRequest extends AbstractRequest<GetActiveNodesResponse> {

	private static final long serialVersionUID = 5976296204245851445L;

	@Override
	public Class<GetActiveNodesResponse> getResponseClass() {
		return GetActiveNodesResponse.class;
	}
}
