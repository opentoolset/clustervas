// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class GMPRequest extends AbstractRequest<GMPResponse> {

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
	public Class<GMPResponse> getResponseClass() {
		return GMPResponse.class;
	}
}
