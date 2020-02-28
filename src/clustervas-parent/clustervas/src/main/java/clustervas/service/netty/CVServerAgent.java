// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import clustervas.CVConfig;
import clustervas.api.netty.MessageDecoder;
import clustervas.api.netty.MessageEncoder;
import clustervas.utils.CVLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

@Component
public class CVServerAgent {

	@Autowired
	private CVServerInboundMessageHandler inboundMessageHandler;

	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();

	public boolean openChannel() {

		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workerGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(encoder, decoder, inboundMessageHandler);
				}
			});

			ChannelFuture channelFuture = null;
			while ((channelFuture = connectSafe(bootstrap)) == null) {
				TimeUnit.SECONDS.sleep(1);
			}

			// channelFuture.channel().closeFuture().sync();
			return true;
		} catch (InterruptedException e) {
			CVLogger.warn(e);
		} finally {
			workerGroup.shutdownGracefully();
		}

		return false;
	}

	private ChannelFuture connectSafe(Bootstrap bootstrap) throws InterruptedException {
		try {
			return bootstrap.connect(CVConfig.getManagerHost(), CVConfig.getManagerPort()).sync();
		} catch (Exception e) {
			CVLogger.debug(e, e.getLocalizedMessage());
		}

		return null;
	}

	// ---

	@PostConstruct
	private void init() {

	}
}
