// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientAgent extends AbstractAgent {

	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private Bootstrap bootstrap = new Bootstrap();

	private Config config = new Config();

	private PeerContext peerContext = new PeerContext();

	private boolean shutdownRequested = false;

	// ---

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
					ch.pipeline().addLast(new MessageEncoder(), new MessageDecoder(), new InboundMessageHandler(context));
					ch.pipeline().addLast(new ChannelHandler() {

						@Override
						public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
							ClientAgent.this.peerContext.setChannelHandlerContext(ctx);
						}

						@Override
						public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
							ctx.close();
							ClientAgent.this.peerContext.setChannelHandlerContext(null);
						}

						@Override
						public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
						}
					});
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

			ChannelHandlerContext channelHandlerContext = this.peerContext.getChannelHandlerContext();
			if (channelHandlerContext != null) {
				channelHandlerContext.close();
			}

			this.workerGroup.shutdownGracefully();
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
		}
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request) {
		return this.context.getMessageSender().doRequest(request, this.peerContext);
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, long timeoutMillis) {
		return this.context.getMessageSender().doRequest(request, this.peerContext, timeoutMillis);
	}

	public void sendMessage(AbstractMessage message) {
		this.context.getMessageSender().sendMessage(message, this.peerContext);
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
