// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class RemoveNodeResponse extends AbstractRequest<LoadNewNodeResponse> {

	private static final long serialVersionUID = -4741917937426676207L;

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
