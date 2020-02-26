// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import clustervas.CVContext.Mode;
import clustervas.api.CVService;
import clustervas.api.MessageDecoder;
import clustervas.api.MessageEncoder;
import clustervas.api.messages.SampleRequest;
import clustervas.api.messages.SampleResponse;
import clustervas.service.netty.CVNettyClient;
import clustervas.utils.Logger;
import io.netty.bootstrap.ServerBootstrap;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class MTCVNettyClient {

	static {
		CVContext.mode = Mode.UNIT_TEST;
	}

	@Autowired
	private CVNettyClient client;

	@BeforeClass
	public static void beforeClass() throws Exception {
		new Thread(() -> TestNettyServer.run()).start();
	}

	@Test
	public void testConnect() throws Exception {
		Assert.assertTrue(client.openChannel());
	}

	private static class TestNettyServer {

		private static int port = CVConfig.getManagerPort();

		public static void run() {
			EventLoopGroup bossGroup = new NioEventLoopGroup();
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			try {
				ServerBootstrap serverBootstrap = new ServerBootstrap();
				serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new MessageDecoder(), new MessageEncoder(), new OutboundHandler(), new InboundHandler());
					}
				}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

				ChannelFuture f = serverBootstrap.bind(port).sync();
				f.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				Logger.error(e);
			} finally {
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		}

		private static class OutboundHandler extends ChannelOutboundHandlerAdapter {

			@Override
			public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
				super.write(ctx, msg, promise);
			}
		}

		private static class InboundHandler extends ChannelInboundHandlerAdapter {

			@Override
			public void channelActive(ChannelHandlerContext ctx) throws Exception {
			}

			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			}
		}

		private static class CVServiceConsumer implements CVService {

			@Override
			public SampleResponse getSampleResponse(SampleRequest request) {
				return null;
			}
		}
	}
}
