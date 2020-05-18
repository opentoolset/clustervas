// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class SyncOperationRequest extends AbstractRequest<SyncOperationResponse> {

	public enum Type {
		DO_INTERNAL_SYNC,
		DO_POST_SYNC_OPERATIONS;
	}

	private Type type;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public Class<SyncOperationResponse> getResponseClass() {
		return SyncOperationResponse.class;
	}
}
