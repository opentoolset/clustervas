package clustervas.api.netty.agent;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.CVApiConstants;
import clustervas.api.netty.CVApiContext;
import clustervas.api.netty.CVApiContext.OperationContext;
import clustervas.api.netty.CVOutboundMessageHandler;
import clustervas.api.netty.MessageDecoder;
import clustervas.api.netty.MessageEncoder;
import clustervas.api.netty.MessageWrapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class CVClientAgent {

	private Logger logger = CVApiContext.getLogger();

	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();

	private EventLoopGroup bossGroup = new NioEventLoopGroup();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private ServerBootstrap bootstrap = new ServerBootstrap();

	public <TReq extends AbstractMessage, TResp extends AbstractMessage> TResp doRequest(TReq request, Class<TResp> classOfResponse) {
		try {
			ChannelHandlerContext channelHandlerContext = null;
			while ((channelHandlerContext = CVApiContext.getInstance().getChannelHandlerContext()) == null) {
				TimeUnit.SECONDS.sleep(1);
			}

			MessageWrapper requestWrapper = MessageWrapper.createRequest(request);
			Thread currentThread = Thread.currentThread();

			OperationContext operationContext = new OperationContext();
			operationContext.setThread(currentThread);

			Map<String, OperationContext> waitingRequests = CVApiContext.getInstance().getWaitingRequests();
			waitingRequests.put(requestWrapper.getId(), operationContext);

			channelHandlerContext.writeAndFlush(requestWrapper).sync();
			synchronized (currentThread) {
				currentThread.wait(CVApiConstants.REQUST_TIMEOUT_MILLIS);
			}

			operationContext = waitingRequests.remove(requestWrapper.getId());
			MessageWrapper responseWrapper = operationContext.getResponseWrapper();
			if (responseWrapper != null) {
				TResp responseMessage = responseWrapper.deserializeMessage(classOfResponse);
				return responseMessage;
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
		}

		return null;
	}

	public boolean startup() {
		try {
			this.bootstrap.group(this.bossGroup, this.workerGroup);
			this.bootstrap.channel(NioServerSocketChannel.class);
			this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					try {
						ch.pipeline().addLast(CVClientAgent.this.encoder, CVClientAgent.this.decoder, new CVClientInboundMessageHandler(), new CVOutboundMessageHandler());
					} catch (Exception e) {
						logger.debug(e.getLocalizedMessage(), e);
					}
				}
			});

			this.bootstrap.option(ChannelOption.SO_BACKLOG, 128);
			this.bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			this.bootstrap.localAddress(new InetSocketAddress(CVApiConstants.DEFAULT_MANAGER_HOST, CVApiConstants.DEFAULT_MANAGER_PORT));

			ChannelFuture channelFuture = this.bootstrap.bind().sync();
			new Thread(() -> close(channelFuture)).start();
			return true;
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
		}

		return false;
	}

	public void shutdown() {
		this.bossGroup.shutdownGracefully();
		this.workerGroup.shutdownGracefully();
	}

	private void close(ChannelFuture channelFuture) {
		try {
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
		}
		logger.info("Closed");
	}
}