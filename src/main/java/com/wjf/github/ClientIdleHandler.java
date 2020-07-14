package com.wjf.github;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;


public class ClientIdleHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = Logger.getLogger(ClientIdleHandler.class);

	private int i = 0;

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent stateEvent = (IdleStateEvent) evt;
			if (stateEvent.state() == IdleState.WRITER_IDLE) {
				log.info("write idle!");
			}
			if (stateEvent.state() == IdleState.READER_IDLE) {
				log.info("reader idle!");
			}
			if (stateEvent.state() == IdleState.ALL_IDLE) {
				log.info("all idle!");
			}
			log.info("心跳维持！");
			ctx.channel().writeAndFlush(createKeepAliveRequest());
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	private ProtoMsg.MyProtoMsg createKeepAliveRequest() {
//		while (MyNettyClient.sessionId == null || MyNettyClient.token == null) {
//			log.error("尚未登录！");
//		}
		ProtoMsg.KeepAlive_Request.Builder keepAliveRequest = ProtoMsg.KeepAlive_Request.newBuilder();
		keepAliveRequest.setUid(MyNettyClient.uid);
		keepAliveRequest.setSessionId(MyNettyClient.sessionId);
		keepAliveRequest.setToken(MyNettyClient.token);
		keepAliveRequest.setMsg("ping!i=" + i++);

		ProtoMsg.MyProtoMsg.Builder protoMsgBuilder = ProtoMsg.MyProtoMsg.newBuilder();
		protoMsgBuilder.setHeadType(ProtoMsg.HeadType.KEEPALIVE_REQUEST);
		protoMsgBuilder.setKeepaliveRequest(keepAliveRequest);
		return protoMsgBuilder.build();
	}
}
