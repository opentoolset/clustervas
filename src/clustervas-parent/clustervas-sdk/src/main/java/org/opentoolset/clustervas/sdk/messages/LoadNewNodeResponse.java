package org.opentoolset.clustervas.sdk.messages;

public class LoadNewNodeResponse extends AbstractResponse {

	private static final long serialVersionUID = 4639909206630009303L;

	private String nodeName;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
}
