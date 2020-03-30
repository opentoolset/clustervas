// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class NodeManagerInfoRequest extends AbstractRequest<NodeManagerInfoResponse> {

	private static final long serialVersionUID = -5600145059168127507L;

	@Override
	public Class<NodeManagerInfoResponse> getResponseClass() {
		return NodeManagerInfoResponse.class;
	}
}
