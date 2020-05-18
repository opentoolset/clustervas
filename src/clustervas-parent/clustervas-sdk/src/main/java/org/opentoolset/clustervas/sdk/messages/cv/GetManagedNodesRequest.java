// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages.cv;

public class GetManagedNodesRequest extends AbstractRequestFromNodeManager<GetManagedNodesResponse> {

	@Override
	public Class<GetManagedNodesResponse> getResponseClass() {
		return GetManagedNodesResponse.class;
	}
}
