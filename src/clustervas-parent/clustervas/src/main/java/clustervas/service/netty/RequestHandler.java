// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import clustervas.api.CVService;
import clustervas.api.MessageType;
import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.AbstractRequestHandler;
import clustervas.api.netty.RequestWrapper;
import clustervas.api.netty.ResponseWrapper;

@Component
public class RequestHandler extends AbstractRequestHandler {

	@Autowired
	private CVService serviceProvider;

	@Override
	protected ResponseWrapper processMessage(RequestWrapper requestWrapper) {
		// AbstractMessage<?> response = null;
		//
		// switch (requestWrapper.getType().getType()) {
		// case SAMPLE_REQUEST: {
		// response = serviceProvider.getSampleResponse(request);
		// break;
		// }
		//
		// default:
		// break;
		// }

		AbstractMessage request = requestWrapper.getMessage();

		MessageType<AbstractMessage> type = requestWrapper.getType();
		AbstractMessage response = type.getRequestProcessor().apply(request, serviceProvider);

		ResponseWrapper responseWrapper = new ResponseWrapper(response);
		return responseWrapper;
	}
}