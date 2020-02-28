package clustervas.api.netty.agent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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
	private static Channel channel;

	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					try {
						ch.pipeline().addLast(new MessageDecoder(), new MessageEncoder(), new CVClientInboundMessageHandler());
					} catch (Exception e) {
						CVApiContext.getLogger().debug(e.getLocalizedMessage(), e);
					}
				}
			}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = serverBootstrap.bind(port).sync();
			channel = f.channel();
			// channel.closeFuture().sync();

			while (true) {
				TimeUnit.SECONDS.sleep(1);
			}
		} catch (InterruptedException e) {
			CVApiContext.getLogger().error("Interrupted", e);
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

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
				CVApiContext.getLogger().error("Interrupted", e);
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
}