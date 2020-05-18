package org.opentoolset.clustervas.sdk.messages;

public class LoadNewContainerResponse extends AbstractResponse {

	private String containerName;

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
}
