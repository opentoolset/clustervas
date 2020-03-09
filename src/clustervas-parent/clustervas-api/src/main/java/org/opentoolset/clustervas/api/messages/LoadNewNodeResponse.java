package org.opentoolset.clustervas.api.messages;

import org.opentoolset.clustervas.net.AbstractMessage;

public class LoadNewNodeResponse extends AbstractMessage {

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
