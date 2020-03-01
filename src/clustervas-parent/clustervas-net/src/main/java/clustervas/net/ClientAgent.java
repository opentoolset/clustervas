// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientAgent {

	private static Logger logger = Context.getLogger();

	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private Bootstrap bootstrap = new Bootstrap();

	private Context context = new Context();
	private InboundMessageHandler inboundMessageHandler = new InboundMessageHandler(context);

	private Config config = new Config();

	private boolean shutdownRequested = false;

	// ---

	public Context getContext() {
		return context;
	}

	/**
	 * Configuration object including configuration parameters for this agent.<br />
	 * Configuration parameters can be changed if needed. <br />
	 * All configuration adjustments should be made before calling the method "startup".
	 * 
	 * @return Configuration object
	 */
	public Config getConfig() {
		return config;
	}

	public void startup() {
		this.shutdownRequested = false;

		this.bootstrap.group(workerGroup);
		this.bootstrap.channel(NioSocketChannel.class);
		this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				try {
					ch.pipeline().addLast(encoder, decoder, inboundMessageHandler);
				} catch (Exception e) {
					logger.debug(e.getLocalizedMessage(), e);
				}
			}
		});

		this.bootstrap.remoteAddress(new InetSocketAddress(config.getRemoteHost(), config.getRemotePort()));
		new Thread(() -> maintainConnection()).start();
	}

	public void shutdown() {
		try {
			this.shutdownRequested = true;
			this.context.getMessageSender().shutdown();
			this.workerGroup.shutdownGracefully();
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
		}
	}

	// ---

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
			logger.debug(e.getLocalizedMessage(), e);
		}
	}

	private ChannelFuture connectSafe() throws InterruptedException {
		try {
			if (!shutdownRequested) {
				return this.bootstrap.connect().sync();
			}
		} catch (Exception e) {
			logger.debug(e.getLocalizedMessage(), e);
		}

		return null;
	}

	// ---

	public class Config {

		private String remoteHost = Constants.DEFAULT_SERVER_HOST;
		private int remotePort = Constants.DEFAULT_SERVER_PORT;

		// ---

		public String getRemoteHost() {
			return remoteHost;
		}

		public int getRemotePort() {
			return remotePort;
		}

		// ---

		public Config setRemoteHost(String remoteHost) {
			this.remoteHost = remoteHost;
			return this;
		}

		public Config setRemotePort(int remotePort) {
			this.remotePort = remotePort;
			return this;
		}
	}
}
