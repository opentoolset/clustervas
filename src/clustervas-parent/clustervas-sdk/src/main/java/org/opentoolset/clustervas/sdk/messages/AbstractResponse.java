package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractMessage;

public abstract class AbstractResponse extends AbstractMessage {

	private boolean successfull = false;

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(boolean successfull) {
		this.successfull = successfull;
	}
}
