// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<MessageWrapper<AbstractMessage>> {

	@Override
	protected void encode(ChannelHandlerContext ctx, MessageWrapper<AbstractMessage> messageWrapper, ByteBuf out) throws Exception {
		String serializedMessageWrapper = messageWrapper.serialize();
		out.writeCharSequence(serializedMessageWrapper, CVApiConstants.DEFAULT_CHARSET);
	}
}