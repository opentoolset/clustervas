package org.opentoolset.clustervas.sdk.messages.cv;

import org.opentoolset.nettyagents.AbstractMessage;
import org.opentoolset.nettyagents.AbstractRequest;

public abstract class AbstractRequestFromNodeManager<T extends AbstractMessage> extends AbstractRequest<T> {

	private String senderId;

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
}
