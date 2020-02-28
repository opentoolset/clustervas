// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.messages;

import clustervas.api.MessageType;
import clustervas.api.MessageTypes;
import clustervas.api.netty.AbstractMessage;

public class SampleRequest extends AbstractMessage {

	@Override
	@SuppressWarnings("unchecked")
	public MessageType<SampleRequest> getType() {
		return MessageTypes.SAMPLE_REQUEST;
	}
}
