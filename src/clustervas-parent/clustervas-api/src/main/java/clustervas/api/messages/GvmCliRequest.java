// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.messages;

import clustervas.net.AbstractRequest;

public class GvmCliRequest extends AbstractRequest<GvmCliResponse> {

	private String nodeName;

	private String xml;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	@Override
	public Class<GvmCliResponse> getResponseClass() {
		return GvmCliResponse.class;
	}
}
