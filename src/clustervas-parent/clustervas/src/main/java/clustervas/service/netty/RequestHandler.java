// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import clustervas.api.MessageTypes;
import clustervas.api.messages.SampleRequest;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.AbstractRequestHandler;
import clustervas.api.netty.RequestWrapper;
import clustervas.api.netty.ResponseWrapper;

@Component
public class RequestHandler extends AbstractRequestHandler {

	@Autowired
	private CVServiceProvider serviceProvider;

	@Override
	protected ResponseWrapper processMessage(RequestWrapper requestWrapper) {
		AbstractMessage<?> response = null;

		switch (requestWrapper.getType().getType()) {
			case SAMPLE_REQUEST: {
				SampleRequest request = requestWrapper.getMessage(MessageTypes.SAMPLE_REQUEST.getMessageClass());
				response = serviceProvider.getSampleResponse(request);
				break;
			}

			default:
				break;
		}

		ResponseWrapper responseWrapper = new ResponseWrapper(response);
		return responseWrapper;
	}
}