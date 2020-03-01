// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import org.slf4j.Logger;

import clustervas.net.Context.OperationContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InboundMessageHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = Context.getLogger();

	private Context context;

	// ---

	public InboundMessageHandler(Context context) {
		this.context = context;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		context.getMessageSender().setChannelHandlerContext(ctx);
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
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getLocalizedMessage(), cause);
		// ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof MessageWrapper) {
			MessageWrapper messageWrapper = (MessageWrapper) msg;

			String correlationId = messageWrapper.getCorrelationId();
			if (correlationId != null) {
				OperationContext operationContext = context.getMessageSender().getWaitingRequests().get(correlationId);
				if (operationContext != null) {
					operationContext.setResponseWrapper(messageWrapper);
					Thread thread = operationContext.getThread();
					synchronized (thread) {
						if (thread.isAlive()) {
							thread.notify();
						}
					}
				} else {
					logger.warn("Response was ignored because of timeout");
				}
			} else {
				String id = messageWrapper.getId();
				if (id != null) {
					AbstractMessage response = context.getMessageReceiver().handleRequest(messageWrapper);
					MessageWrapper responseWrapper = MessageWrapper.createResponse(response, id);
					ctx.writeAndFlush(responseWrapper);
				} else {
					context.getMessageReceiver().handleMessage(messageWrapper);
				}
			}
		} else {
			logger.warn("Message couldn't be recognized");
		}
	}
}