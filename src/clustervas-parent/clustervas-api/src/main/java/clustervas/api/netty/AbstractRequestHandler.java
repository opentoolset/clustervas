// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class AbstractRequestHandler extends ChannelInboundHandlerAdapter {

	protected abstract ResponseWrapper processMessage(RequestWrapper requestWrapper);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof RequestWrapper) {
			RequestWrapper requestWrapper = (RequestWrapper) msg;
			ResponseWrapper responseWrapper = processMessage(requestWrapper);
			ChannelFuture future = ctx.writeAndFlush(responseWrapper);
		} else {
			Context.getInstance().getLogger().warn("Request was not recognized");
		}
	}
}