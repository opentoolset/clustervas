package clustervas.api.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class CVNettyConnectionAcceptor {

	private static int port = CVApiConstants.DEFAULT_MANAGER_PORT;
	private static Channel channel;

	private Map<String, OperationContext> waitingRequests = new ConcurrentHashMap<>();

	public Map<String, OperationContext> getWaitingRequests() {
		return waitingRequests;
	}

	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new MessageDecoder(), new MessageEncoder(), new OutboundHandler(), new InboundHandler(CVNettyConnectionAcceptor.this));
				}
			}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = serverBootstrap.bind(port).sync();
			channel = f.channel();
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			Context.getInstance().getLogger().error("Interrupted", e);
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

		this.waitingRequests.put(requestWrapper.getId(), operationContext);
		channel.write(requestWrapper);
		try {
			currentThread.wait(CVApiConstants.REQUST_TIMEOUT_MILLIS);
		} catch (InterruptedException e) {
			Context.getInstance().getLogger().error("Interrupted", e);
		}

		operationContext = this.waitingRequests.remove(requestWrapper.getId());
		ResponseWrapper responseWrapper = operationContext.getResponseWrapper();
		if (responseWrapper != null) {
			TResp responseMessage = responseWrapper.deserializeMessage(classOfResponse);
			return responseMessage;
		}

		return null;
	}

	// ---

	private static class OutboundHandler extends ChannelOutboundHandlerAdapter {

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			super.write(ctx, msg, promise);
		}
	}

	private static class InboundHandler extends ChannelInboundHandlerAdapter {

		private CVNettyConnectionAcceptor agent;

		public InboundHandler(CVNettyConnectionAcceptor agent) {
			this.agent = agent;
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof ResponseWrapper) {
				ResponseWrapper responseWrapper = (ResponseWrapper) msg;
				String requestId = responseWrapper.getRequestId();
				if (requestId != null) {
					OperationContext operationContext = this.agent.getWaitingRequests().get(requestId);
					if (operationContext != null) {
						operationContext.setResponseWrapper(responseWrapper);
						Thread thread = operationContext.getThread();
						if (thread.isAlive()) {
							thread.notify();
						}
					} else {
						Context.getInstance().getLogger().warn("Response was ignored because of timeout");
					}
				} else {
					Context.getInstance().getLogger().warn("Response has a null requestID");
				}
			} else {
				Context.getInstance().getLogger().warn("Response is expected, message was {}", msg);
			}
		}
	}

	private static class OperationContext {

		private ResponseWrapper responseWrapper;
		private Thread thread;

		public ResponseWrapper getResponseWrapper() {
			return responseWrapper;
		}

		public void setResponseWrapper(ResponseWrapper responseWrapper) {
			this.responseWrapper = responseWrapper;
		}

		public Thread getThread() {
			return thread;
		}

		public void setThread(Thread thread) {
			this.thread = thread;
		}
	}
}