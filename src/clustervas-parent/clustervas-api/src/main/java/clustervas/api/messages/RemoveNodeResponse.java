// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.messages;

import clustervas.net.AbstractRequest;

public class RemoveNodeResponse extends AbstractRequest<LoadNewNodeResponse> {

	private boolean successfull = false;

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(boolean successfull) {
		this.successfull = successfull;
	}

	@Override
	public Class<LoadNewNodeResponse> getResponseClass() {
		return LoadNewNodeResponse.class;
	}
}
