// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import clustervas.api.MessageTypes;
import clustervas.api.messages.SampleRequest;
import clustervas.api.netty.MessageWrapper;
import clustervas.utils.Logger;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Component
public class RequestHandler extends ChannelInboundHandlerAdapter {

	@Autowired
	private CVServiceProvider serviceProvider;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof MessageWrapper) {
			MessageWrapper requestWrapper = (MessageWrapper) msg;
			MessageWrapper responseWrapper = processMessage(requestWrapper);
			ChannelFuture future = ctx.writeAndFlush(responseWrapper);
		} else {
			Logger.warn("Request was not recognized");
		}
	}

	private MessageWrapper processMessage(MessageWrapper requestWrapper) {
		MessageWrapper response = null;

		switch (requestWrapper.getType().getType()) {
			case SAMPLE_REQUEST: {
				SampleRequest request = requestWrapper.getMessage(MessageTypes.SAMPLE_REQUEST.getMessageClass());
				response = new MessageWrapper(serviceProvider.getSampleResponse(request), MessageTypes.SAMPLE_RESPONSE);
				break;
			}

			default:
				break;
		}

		return response;
	}
}