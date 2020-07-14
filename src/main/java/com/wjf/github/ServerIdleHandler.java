package com.wjf.github;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

public class ServerIdleHandler extends ChannelInboundHandlerAdapter {
	private static final Logger log = Logger.getLogger(ServerIdleHandler.class);

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.WRITER_IDLE) {
				log.info("writer idle!");
			}
			if (event.state() == IdleState.READER_IDLE) {
				log.info("reader idle!");
			}
			if (event.state() == IdleState.ALL_IDLE) {
				log.info("all idle");
			}

		}
		super.userEventTriggered(ctx, evt);
	}
}
