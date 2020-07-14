package com.wjf.github;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;


public class ClientChannelHandler extends SimpleChannelInboundHandler<ProtoMsg.MyProtoMsg> {

	private static final Logger log = Logger.getLogger(ClientChannelHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().writeAndFlush(createLoginRequest());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			ProtoMsg.MyProtoMsg protoMsg = (ProtoMsg.MyProtoMsg) msg;
			if (protoMsg.getHeadType() == null) {
				log.error("未知的信息类型！" + protoMsg.toString());
				return;
			}
			switch (protoMsg.getHeadType()) {
				case MESSAGE_RESPONSE:
					log.info("接收到服务器的消息响应信息：" + protoMsg.getMessageResponse().getMsg());
					break;
				case LOGOUT_RESPONSE:
					log.info("接收到服务器的登出相应信息：" + protoMsg.getLogoutResponse().getMsg());
					break;
				case MESSAGE_NOTICE:
					log.info("接收到服务器的消息推送信息：" + protoMsg.getMessageNotice().getContent());
					break;
				case LOGIN_RESPONSE:
					log.info("接收到服务器的登录响应信息：" + protoMsg.getLoginResponse().getMsg());
					if (protoMsg.getLoginResponse().getResult()) {
						MyNettyClient.token = protoMsg.getLoginResponse().getToken();
						MyNettyClient.sessionId = protoMsg.getLoginResponse().getSessionId();
					}
					break;
				case KEEPALIVE_RESPONSE:
					log.info("接收到服务器的心跳相应信息：" + protoMsg.getKeepaliveResponse().getMsg());
					break;
				case UNRECOGNIZED:
				case KEEPALIVE_REQUEST:
				case LOGIN_REQUEST:
				case LOGOUT_REQUEST:
				case MESSAGE_REQUEST:
					log.error("意料外的消息类型！" + protoMsg.toString());
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProtoMsg.MyProtoMsg msg) throws Exception {

	}

	private ProtoMsg.MyProtoMsg createLoginRequest() {
		ProtoMsg.Login_Request.Builder loginRequest = ProtoMsg.Login_Request.newBuilder();
		loginRequest.setUid(MyNettyClient.uid);
		loginRequest.setDeviceNum(MyNettyClient.uid);
		loginRequest.setPlatform(1);
		ProtoMsg.MyProtoMsg.Builder protoMsgBuilder = ProtoMsg.MyProtoMsg.newBuilder();
		protoMsgBuilder.setHeadType(ProtoMsg.HeadType.LOGIN_REQUEST);
		protoMsgBuilder.setLoginRequest(loginRequest);
		return protoMsgBuilder.build();
	}

}
