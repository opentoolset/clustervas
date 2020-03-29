package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractMessage;

public class LoadNewNodeResponse extends AbstractMessage {

	private static final long serialVersionUID = 4639909206630009303L;

	private boolean successfull = false;

	private String nodeName;

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(boolean successfull) {
		this.successfull = successfull;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
}
