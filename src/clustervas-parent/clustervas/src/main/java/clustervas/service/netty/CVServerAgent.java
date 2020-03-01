// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import clustervas.CVConfig;
import clustervas.api.netty.CVOutboundMessageHandler;
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

	private EventLoopGroup workerGroup = new NioEventLoopGroup();

	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();

	private Bootstrap bootstrap = new Bootstrap();

	// ---

	public boolean openChannel() {

		try {
			this.bootstrap.group(workerGroup);
			this.bootstrap.channel(NioSocketChannel.class);
			this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					try {
						ch.pipeline().addLast(encoder, decoder, inboundMessageHandler, new CVOutboundMessageHandler());
					} catch (Exception e) {
						CVLogger.debug(e, e.getLocalizedMessage());
					}
				}
			});

			this.bootstrap.remoteAddress(new InetSocketAddress(CVConfig.getManagerHost(), CVConfig.getManagerPort()));

			ChannelFuture channelFuture = null;
			while ((channelFuture = connectSafe()) == null) {
				TimeUnit.SECONDS.sleep(1);
			}

			ChannelFuture cf = channelFuture;
			new Thread(() -> close(cf)).start();

			return true;
		} catch (InterruptedException e) {
			CVLogger.warn(e, "Interrupted");
		}

		return false;
	}

	public void shutdown() {
		try {
			this.workerGroup.shutdownGracefully();
		} catch (Exception e) {
			CVLogger.warn(e);
		}
	}

	// ---

	@PostConstruct
	private void postConstruct() {
	}

	@PreDestroy
	private void preDestroy() {
		shutdown();
	}

	private ChannelFuture connectSafe() throws InterruptedException {
		try {
			return this.bootstrap.connect().sync();
		} catch (Exception e) {
			CVLogger.debug(e, e.getLocalizedMessage());
		}

		return null;
	}

	private void close(ChannelFuture channelFuture) {
		try {
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			CVLogger.error("Interrupted", e);
		}
		CVLogger.info("Closed");
	}
}
