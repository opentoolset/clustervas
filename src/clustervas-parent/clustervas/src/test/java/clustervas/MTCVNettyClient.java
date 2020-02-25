// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import clustervas.CVContext.Mode;
import clustervas.service.netty.CVNettyClient;
import clustervas.service.netty.RequestData;
import clustervas.service.netty.ResponseData;
import clustervas.utils.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

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
						ch.pipeline().addLast(new RequestDecoder(), new ResponseDataEncoder(), new ProcessingHandler());
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

		private static class RequestDecoder extends ReplayingDecoder<RequestData> {

			private final Charset charset = Charset.forName("UTF-8");

			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

				RequestData data = new RequestData();
				data.setIntValue(in.readInt());
				int strLen = in.readInt();
				data.setStringValue(in.readCharSequence(strLen, charset).toString());
				out.add(data);
			}
		}

		private static class ResponseDataEncoder extends MessageToByteEncoder<ResponseData> {

			@Override
			protected void encode(ChannelHandlerContext ctx, ResponseData msg, ByteBuf out) throws Exception {
				out.writeInt(msg.getIntValue());
			}
		}

		private static class ProcessingHandler extends ChannelInboundHandlerAdapter {

			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

				RequestData requestData = (RequestData) msg;
				ResponseData responseData = new ResponseData();
				responseData.setIntValue(requestData.getIntValue() * 2);
				ChannelFuture future = ctx.writeAndFlush(responseData);
				future.addListener(ChannelFutureListener.CLOSE);
				System.out.println(requestData);
			}
		}
	}
}
