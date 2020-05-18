// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages.cv;

public class GetManagedContainersRequest extends AbstractRequestFromNodeManager<GetManagedContainersResponse> {

	@Override
	public Class<GetManagedContainersResponse> getResponseClass() {
		return GetManagedContainersResponse.class;
	}
}
