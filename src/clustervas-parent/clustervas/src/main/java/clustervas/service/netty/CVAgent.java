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
public class CVAgent {

	@Autowired
	private CVAgentRequestHandler requestHandler;

	@Autowired
	private CVServerApiContext context;

	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private Bootstrap bootstrap = new Bootstrap();

	private boolean shutdownRequested = false;

	// ---

	public void startup() {
		this.shutdownRequested = false;

		this.bootstrap.group(workerGroup);
		this.bootstrap.channel(NioSocketChannel.class);
		this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				try {
					ch.pipeline().addLast(encoder, decoder, new CVOutboundMessageHandler(), requestHandler);
				} catch (Exception e) {
					CVLogger.debug(e, e.getLocalizedMessage());
				}
			}
		});

		this.bootstrap.remoteAddress(new InetSocketAddress(CVConfig.getManagerHost(), CVConfig.getManagerPort()));
		new Thread(() -> maintainConnection()).start();
	}

	public void shutdown() {
		try {
			this.shutdownRequested = true;
			this.context.getMessageSender().shutdown();
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

	private void maintainConnection() {
		try {
			while (!shutdownRequested) {
				ChannelFuture channelFuture = null;
				while ((channelFuture = connectSafe()) == null && !shutdownRequested) {
					TimeUnit.SECONDS.sleep(1);
				}

				channelFuture.channel().closeFuture().sync();
			}
		} catch (InterruptedException e) {
			CVLogger.warn(e, "Interrupted");
		}
	}

	private ChannelFuture connectSafe() throws InterruptedException {
		try {
			if (!shutdownRequested) {
				return this.bootstrap.connect().sync();
			}
		} catch (Exception e) {
			CVLogger.debug(e, e.getLocalizedMessage());
		}

		return null;
	}
}
