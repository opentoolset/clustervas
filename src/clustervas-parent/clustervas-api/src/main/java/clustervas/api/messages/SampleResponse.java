// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.messages;

import clustervas.api.MessageType;
import clustervas.api.MessageTypes;
import clustervas.api.netty.AbstractMessage;

public class SampleResponse extends AbstractMessage<SampleResponse> {

	@Override
	public MessageType<SampleResponse> getType() {
		return MessageTypes.SAMPLE_RESPONSE;
	}
}
