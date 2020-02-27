// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class AbstractRequestHandler extends ChannelInboundHandlerAdapter {

	protected abstract MessageWrapper processMessage(MessageWrapper requestWrapper);

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
			Context.getInstance().getLogger().warn("Request was not recognized");
		}
	}
}