// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import org.springframework.stereotype.Service;

import clustervas.api.AbstractMessage;
import clustervas.api.CVService;
import clustervas.api.MessageTypes;
import clustervas.api.MessageWrapper;
import clustervas.api.messages.SampleRequest;
import clustervas.api.messages.SampleResponse;

@Service
public class CVServiceProvider implements CVService {

	// ---

	public AbstractMessage processMessage(MessageWrapper message) {
		AbstractMessage response = null;

		switch (message.getType().getType()) {
			case SAMPLE_REQUEST: {
				response = getSampleResponse(message.getMessage(MessageTypes.SAMPLE_REQUEST.getMessageClass()));
				break;
			}

			default:
				break;
		}

		return response;
	}

	// ---

	@Override
	public SampleResponse getSampleResponse(SampleRequest request) {
		return null;
	}
}
