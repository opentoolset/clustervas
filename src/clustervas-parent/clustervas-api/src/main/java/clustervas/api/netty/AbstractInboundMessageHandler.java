// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import clustervas.api.netty.CVApiContext.OperationContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class AbstractInboundMessageHandler extends ChannelInboundHandlerAdapter {

	protected abstract ResponseWrapper processRequest(RequestWrapper requestWrapper);

	protected abstract void processMessage(MessageWrapper<?> messageWrapper);

	// ---

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		CVApiContext.getInstance().setNettyChannel(ctx.channel());
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof RequestWrapper) {
			RequestWrapper requestWrapper = (RequestWrapper) msg;
			ResponseWrapper responseWrapper = processRequest(requestWrapper);
			ctx.writeAndFlush(responseWrapper);
		} else if (msg instanceof ResponseWrapper) {
			ResponseWrapper responseWrapper = (ResponseWrapper) msg;
			String requestId = responseWrapper.getRequestId();
			if (requestId != null) {
				OperationContext operationContext = CVApiContext.getInstance().getWaitingRequests().get(requestId);
				if (operationContext != null) {
					operationContext.setResponseWrapper(responseWrapper);
					Thread thread = operationContext.getThread();
					synchronized (thread) {
						if (thread.isAlive()) {
							thread.notify();
						}
					}
				} else {
					CVApiContext.getLogger().warn("Response was ignored because of timeout");
				}
			} else {
				CVApiContext.getLogger().warn("Response has a null requestID");
			}
		} else if (msg instanceof MessageWrapper) {
			MessageWrapper<?> messageWrapper = (MessageWrapper<?>) msg;
			processMessage(messageWrapper);
		} else {
			CVApiContext.getLogger().warn("Message was not recognized");
		}
	}
}