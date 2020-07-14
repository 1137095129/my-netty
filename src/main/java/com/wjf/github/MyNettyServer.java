package com.wjf.github;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class MyNettyServer {

	public static void main(String[] args) {
		new MyNettyServer().start(9001);
	}

	private void start(int port) {
		EventLoopGroup boss = new NioEventLoopGroup(1);
		EventLoopGroup work = new NioEventLoopGroup(10);
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(boss, work)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel channel) throws Exception {
							channel.pipeline()
									.addLast(new IdleStateHandler(NettyProperties.reader_idle_time * 3, NettyProperties.writer_idle_time * 3, NettyProperties.all_idle_time * 3, NettyProperties.time_unit))
									.addLast(new LengthDecoder())
									.addLast(new ProtobufDecoder())

									.addLast(new ServerIdleHandler())

									.addLast(new LengthEncoder())
									.addLast(new ServerChannelHandler());
						}
					})
					.option(ChannelOption.SO_BACKLOG, 1024);
			ChannelFuture sync = bootstrap.bind(port).sync();
			sync.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				work.shutdownGracefully().sync();
				boss.shutdownGracefully().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
