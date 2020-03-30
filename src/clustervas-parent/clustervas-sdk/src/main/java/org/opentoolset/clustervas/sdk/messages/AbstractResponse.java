package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractMessage;

public abstract class AbstractResponse extends AbstractMessage {

	private static final long serialVersionUID = 1851912817285621749L;

	private boolean successfull = false;

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(boolean successfull) {
		this.successfull = successfull;
	}
}
