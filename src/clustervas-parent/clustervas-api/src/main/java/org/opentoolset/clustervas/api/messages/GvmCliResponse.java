// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.api.messages;

import org.opentoolset.nettyagents.AbstractMessage;

public class GvmCliResponse extends AbstractMessage {

	private static final long serialVersionUID = -5854931190418980436L;

	private boolean successfull = false;

	private String xml;

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(boolean successfull) {
		this.successfull = successfull;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}
}
