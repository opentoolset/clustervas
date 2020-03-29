// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.api.messages;

import org.opentoolset.nettyagents.AbstractRequest;

public class GMPRequest extends AbstractRequest<GMPResponse> {

	private static final long serialVersionUID = 7081137175920247788L;

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
