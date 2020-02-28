// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import clustervas.api.netty.CVApiContext.OperationContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class AbstractInboundMessageHandler extends ChannelInboundHandlerAdapter {

	protected abstract AbstractMessage processMessage(MessageWrapper messageWrapper);

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
		if (msg instanceof MessageWrapper) {
			MessageWrapper messageWrapper = (MessageWrapper) msg;

			String correlationId = messageWrapper.getCorrelationId();
			if (correlationId != null) {
				OperationContext operationContext = CVApiContext.getInstance().getWaitingRequests().get(correlationId);
				if (operationContext != null) {
					operationContext.setResponseWrapper(messageWrapper);
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
				AbstractMessage response = processMessage(messageWrapper);

				String id = messageWrapper.getId();
				if (id != null) {
					MessageWrapper responseWrapper = MessageWrapper.createResponse(response, id);
					ctx.writeAndFlush(responseWrapper);
				}
			}
		} else {
			CVApiContext.getLogger().warn("Message couldn't be recognized");
		}
	}
}