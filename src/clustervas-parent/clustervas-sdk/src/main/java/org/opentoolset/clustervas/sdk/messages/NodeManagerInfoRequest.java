// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class NodeManagerInfoRequest extends AbstractRequest<NodeManagerInfoResponse> {

	@Override
	public Class<NodeManagerInfoResponse> getResponseClass() {
		return NodeManagerInfoResponse.class;
	}
}
