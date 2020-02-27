// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.messages;

import clustervas.api.MessageTypes;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.MessageType;

public class SampleRequest extends AbstractMessage<SampleRequest> {

	@Override
	public MessageType<SampleRequest> getType() {
		return MessageTypes.SAMPLE_REQUEST;
	}
}
