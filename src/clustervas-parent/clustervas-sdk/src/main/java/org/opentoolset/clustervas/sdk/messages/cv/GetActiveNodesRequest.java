// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages.cv;

public class GetActiveNodesRequest extends AbstractRequestFromNodeManager<GetActiveNodesResponse> {

	private static final long serialVersionUID = 5976296204245851445L;

	@Override
	public Class<GetActiveNodesResponse> getResponseClass() {
		return GetActiveNodesResponse.class;
	}
}
