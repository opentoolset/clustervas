package clustervas.api.netty.agent;

import org.slf4j.Logger;

import clustervas.api.netty.CVApiConstants;
import clustervas.api.netty.CVApiContext;
import clustervas.api.netty.CVOutboundMessageHandler;
import clustervas.api.netty.MessageDecoder;
import clustervas.api.netty.MessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class CVManagerAgent {

	private static Logger logger = CVApiContext.getLogger();

	private CVApiContext context = new CVApiContext();

	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();
	private EventLoopGroup bossGroup = new NioEventLoopGroup();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private ServerBootstrap bootstrap = new ServerBootstrap();

	private boolean shutdownRequested = false;

	public void startup() {
		this.shutdownRequested = false;

		this.bootstrap.group(this.bossGroup, this.workerGroup);
		this.bootstrap.channel(NioServerSocketChannel.class);
		this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				try {
					ch.pipeline().addLast(CVManagerAgent.this.encoder, CVManagerAgent.this.decoder, new CVOutboundMessageHandler(), new CVClientInboundMessageHandler(context));
				} catch (Exception e) {
					logger.debug(e.getLocalizedMessage(), e);
				}
			}
		});

		this.bootstrap.option(ChannelOption.SO_BACKLOG, 128);
		this.bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		// this.bootstrap.localAddress(new InetSocketAddress(CVApiConstants.DEFAULT_MANAGER_HOST, CVApiConstants.DEFAULT_MANAGER_PORT));
		new Thread(() -> maintainConnection()).start();
	}

	private void maintainConnection() {
		try {
			while (!shutdownRequested) {
				ChannelFuture channelFuture = this.bootstrap.bind(CVApiConstants.DEFAULT_MANAGER_PORT).sync();
				channelFuture.channel().closeFuture().sync();
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
		}
	}

	public CVApiContext getContext() {
		return context;
	}

	public void shutdown() {
		this.context.getMessageSender().shutdown();
		this.bossGroup.shutdownGracefully();
		this.workerGroup.shutdownGracefully();
	}
}