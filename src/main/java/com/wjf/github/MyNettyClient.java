package com.wjf.github;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class MyNettyClient {

	public static final String uid = "uid_001";
	public volatile static String sessionId = null;
	public volatile static String token = null;

	public static void main(String[] args) {
		new MyNettyClient().start("localhost", 9001);
	}

	private void start(String host, int port) {
		EventLoopGroup group = new NioEventLoopGroup(10);
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel channel) throws Exception {
							channel.pipeline()
									.addLast(new IdleStateHandler(NettyProperties.reader_idle_time, NettyProperties.writer_idle_time, NettyProperties.all_idle_time, NettyProperties.time_unit))
									.addLast(new LengthDecoder())
									.addLast(new ProtobufDecoder())

									.addLast(new LengthEncoder())
									.addLast(new ClientChannelHandler())

									.addLast(new ClientIdleHandler());
						}
					})
					.option(ChannelOption.SO_BACKLOG, 1024);
			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				group.shutdownGracefully().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
