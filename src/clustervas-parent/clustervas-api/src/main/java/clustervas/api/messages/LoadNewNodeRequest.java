// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.messages;

import clustervas.net.AbstractRequest;

public class LoadNewNodeRequest extends AbstractRequest<LoadNewNodeResponse> {

	@Override
	public Class<LoadNewNodeResponse> getResponseClass() {
		return LoadNewNodeResponse.class;
	}
}
