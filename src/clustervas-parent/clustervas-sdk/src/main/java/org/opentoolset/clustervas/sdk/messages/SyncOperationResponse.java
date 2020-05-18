package org.opentoolset.clustervas.sdk.messages;

public class SyncOperationResponse extends AbstractResponse {

	private String nodeName;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
}
