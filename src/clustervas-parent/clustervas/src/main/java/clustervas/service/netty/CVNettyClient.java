// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import clustervas.CVConfig;
import clustervas.api.netty.MessageDecoder;
import clustervas.api.netty.MessageEncoder;
import clustervas.utils.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

@Component
public class CVNettyClient {

	public boolean openChannel() {

		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new MessageEncoder(), new MessageDecoder(), new RequestHandler());
				}
			});

			ChannelFuture f = b.connect(CVConfig.getManagerHost(), CVConfig.getManagerPort()).sync();

			f.channel().closeFuture().sync();
			return true;
		} catch (InterruptedException e) {
			Logger.warn(e);
		} finally {
			workerGroup.shutdownGracefully();
		}

		return false;
	}

	// ---

	@PostConstruct
	private void init() {

	}
}
