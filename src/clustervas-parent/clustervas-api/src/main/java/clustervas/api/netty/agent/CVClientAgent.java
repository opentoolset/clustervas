package clustervas.api.netty.agent;

import java.util.Map;

import org.slf4j.Logger;

import clustervas.api.netty.AbstractMessage;
import clustervas.api.netty.CVApiConstants;
import clustervas.api.netty.CVApiContext;
import clustervas.api.netty.CVApiContext.OperationContext;
import clustervas.api.netty.MessageDecoder;
import clustervas.api.netty.MessageEncoder;
import clustervas.api.netty.RequestWrapper;
import clustervas.api.netty.ResponseWrapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class CVClientAgent {

	private static int port = CVApiConstants.DEFAULT_MANAGER_PORT;

	private Logger logger = CVApiContext.getLogger();

	private MessageEncoder encoder = new MessageEncoder();
	private MessageDecoder decoder = new MessageDecoder();

	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private EventLoopGroup bossGroup = new NioEventLoopGroup();
	private ServerBootstrap bootstrap = new ServerBootstrap();
	private Channel channel;

	public <TReq extends AbstractMessage, TResp extends AbstractMessage> TResp doRequest(TReq request, Class<TResp> classOfResponse) {
		RequestWrapper requestWrapper = new RequestWrapper(request);
		Thread currentThread = Thread.currentThread();

		OperationContext operationContext = new OperationContext();
		operationContext.setThread(currentThread);

		Map<String, OperationContext> waitingRequests = CVApiContext.getInstance().getWaitingRequests();
		waitingRequests.put(requestWrapper.getId(), operationContext);
		channel.writeAndFlush(requestWrapper);
		synchronized (currentThread) {
			try {
				currentThread.wait(CVApiConstants.REQUST_TIMEOUT_MILLIS);
			} catch (InterruptedException e) {
				logger.error("Interrupted", e);
			}
		}

		operationContext = waitingRequests.remove(requestWrapper.getId());
		ResponseWrapper responseWrapper = operationContext.getResponseWrapper();
		if (responseWrapper != null) {
			TResp responseMessage = responseWrapper.deserializeMessage(classOfResponse);
			return responseMessage;
		}

		return null;
	}

	public boolean startup() {
		try {
			this.bootstrap.group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					try {
						ch.pipeline().addLast(CVClientAgent.this.encoder, CVClientAgent.this.decoder, new CVClientInboundMessageHandler());
					} catch (Exception e) {
						logger.debug(e.getLocalizedMessage(), e);
					}
				}
			}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture channelFuture = this.bootstrap.bind(port).sync();
			this.channel = channelFuture.channel();
			return true;
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
		}

		return false;
	}

	public void shutdown() {
		this.workerGroup.shutdownGracefully();
		this.bossGroup.shutdownGracefully();
	}
}