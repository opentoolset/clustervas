package org.opentoolset.clustervas.api.messages.cv;

import org.opentoolset.nettyagents.AbstractMessage;
import org.opentoolset.nettyagents.AbstractRequest;

public abstract class AbstractRequestFromNodeManager<T extends AbstractMessage> extends AbstractRequest<T> {

	private static final long serialVersionUID = 6823400532908465245L;

	private String senderId;

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
}
