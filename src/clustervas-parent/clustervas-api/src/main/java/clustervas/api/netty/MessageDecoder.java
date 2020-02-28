// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class MessageDecoder extends ReplayingDecoder<String> {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int readableBytes = in.readableBytes();
		String serializedMessageWrapper = in.readCharSequence(readableBytes, CVApiConstants.DEFAULT_CHARSET).toString();
		MessageWrapper messageWrapper = MessageWrapper.deserialize(serializedMessageWrapper);
		out.add(messageWrapper);
	}
}