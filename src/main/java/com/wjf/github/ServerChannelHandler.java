package com.wjf.github;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerChannelHandler extends SimpleChannelInboundHandler<ProtoMsg.MyProtoMsg> {

	private static final Logger log = Logger.getLogger(ServerChannelHandler.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	public static final Map<String, ChannelAndToken> map = new ConcurrentHashMap<>();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			ProtoMsg.MyProtoMsg protoMsg = (ProtoMsg.MyProtoMsg) msg;

			if (!protoMsg.hasHeadType()) {
				log.error("未找到消息头！" + protoMsg.toString());
				return;
			}

			ProtoMsg.MyProtoMsg responseMsg = null;

			switch (protoMsg.getHeadType()) {
				case MESSAGE_REQUEST:
					log.info("接收到客户端的消息请求信息：" + protoMsg.getMessageRequest().getUid() + "---" + protoMsg.getMessageRequest().getContent());
					responseMsg = createMessageResponse();
					break;
				case LOGOUT_REQUEST:
					log.info("接收到客户端的登出请求信息：" + protoMsg.getMessageRequest().getUid());
					responseMsg = createLogoutResponseInfo(protoMsg.getLogoutRequest().getUid(), protoMsg.getLogoutRequest().getToken());
					break;
				case LOGIN_REQUEST:
					log.info("接收到客户端的登录请求信息：" + protoMsg.getLoginRequest().getUid());
					responseMsg = createLoginResponseInfo(protoMsg.getLoginRequest().getUid(), ctx);
					break;
				case KEEPALIVE_REQUEST:
					log.info("接收到客户端的心跳维持信息：" + protoMsg.getKeepaliveRequest().getMsg());
					responseMsg = createKeepAliveResponse();
					break;
				case UNRECOGNIZED:
					//XXX 未知的消息类型
				case LOGIN_RESPONSE:
					//XXX 服务器接收到登录请求信息，并返回登录响应信息
				case MESSAGE_NOTICE:
					//XXX 服务器端一般不会接收到公告信息
				case LOGOUT_RESPONSE:
					//XXX 服务器接收到登出请求信息，并返回登出响应信息
				case MESSAGE_RESPONSE:
					//XXX 服务器接收到信息请求信息，并返回对应的信息响应信息
				case KEEPALIVE_RESPONSE:
					//XXX 心跳维持应由客户端发送请求信息，服务器返回相应信息
					logError(protoMsg);
			}
			if (responseMsg != null) {
				ctx.channel().writeAndFlush(responseMsg);
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProtoMsg.MyProtoMsg msg) throws Exception {

	}

	private void logError(ProtoMsg.MyProtoMsg protoMsg) {
		log.error("意料外的信息类型:" + protoMsg.toString());
	}

	private ProtoMsg.MyProtoMsg createLoginResponseInfo(String uid, ChannelHandlerContext ctx) {
		String token = IdUtil.randomUUID();
		ProtoMsg.Login_Response.Builder loginResponse = ProtoMsg.Login_Response.newBuilder();
		loginResponse.setResult(true);
		loginResponse.setCode(200);
		loginResponse.setMsg("登陆成功！欢迎你：" + uid);
		loginResponse.setSessionId("session_" + token);
		loginResponse.setToken(token);

		map.put(uid, new ChannelAndToken(token, ctx.channel()));

		ProtoMsg.MyProtoMsg.Builder protoMsgBuilder = ProtoMsg.MyProtoMsg.newBuilder();
		protoMsgBuilder.setHeadType(ProtoMsg.HeadType.LOGIN_RESPONSE);

		protoMsgBuilder.setLoginResponse(loginResponse);
		return protoMsgBuilder.build();
	}

	private ProtoMsg.MyProtoMsg createLogoutResponseInfo(String uid, String token) {

		ProtoMsg.Logout_Response.Builder logoutResponse = ProtoMsg.Logout_Response.newBuilder();
		ChannelAndToken channelAndToken = map.get(uid);
		if (channelAndToken != null && channelAndToken.getToken().equals(token)) {
			logoutResponse.setResult(true);
			logoutResponse.setCode(200);
			logoutResponse.setMsg("登出成功！再见！");
			map.remove(uid);
		} else {
			logoutResponse.setResult(false);
			logoutResponse.setCode(400);
			logoutResponse.setMsg("登出失败！");
		}

		ProtoMsg.MyProtoMsg.Builder protoMsgBuilder = ProtoMsg.MyProtoMsg.newBuilder();
		protoMsgBuilder.setHeadType(ProtoMsg.HeadType.LOGOUT_RESPONSE);
		protoMsgBuilder.setLogoutResponse(logoutResponse);
		return protoMsgBuilder.build();
	}

	private ProtoMsg.MyProtoMsg createKeepAliveResponse() {
		ProtoMsg.KeepAlive_Response.Builder keepAliveResponse = ProtoMsg.KeepAlive_Response.newBuilder();
		keepAliveResponse.setResult(true);
		keepAliveResponse.setCode(200);
		keepAliveResponse.setMsg("pong!");
		ProtoMsg.MyProtoMsg.Builder protoMsgBuilder = ProtoMsg.MyProtoMsg.newBuilder();
		protoMsgBuilder.setHeadType(ProtoMsg.HeadType.KEEPALIVE_RESPONSE);
		protoMsgBuilder.setKeepaliveResponse(keepAliveResponse);
		return protoMsgBuilder.build();
	}

	private ProtoMsg.MyProtoMsg createMessageResponse() {
		ProtoMsg.Message_Response.Builder messageResponse = ProtoMsg.Message_Response.newBuilder();
		messageResponse.setResult(true);
		messageResponse.setCode(200);
		messageResponse.setMsg("已接收到数据！");
		ProtoMsg.MyProtoMsg.Builder protoMsgBuilder = ProtoMsg.MyProtoMsg.newBuilder();
		protoMsgBuilder.setHeadType(ProtoMsg.HeadType.MESSAGE_RESPONSE);
		protoMsgBuilder.setMessageResponse(messageResponse);
		return protoMsgBuilder.build();
	}

	public static ProtoMsg.MyProtoMsg createMessageNotice(int messageType, Object msg, String resourceUrl) throws JsonProcessingException {
		ProtoMsg.Message_Notice.Builder messageNotice = ProtoMsg.Message_Notice.newBuilder();
		messageNotice.setMessageType(messageType);
		messageNotice.setTimestamp(new Date().getTime());
		messageNotice.setJson(mapper.writeValueAsString(msg));
		messageNotice.setResourceUrl(resourceUrl);
		ProtoMsg.MyProtoMsg.Builder protoMsgBuilder = ProtoMsg.MyProtoMsg.newBuilder();
		protoMsgBuilder.setHeadType(ProtoMsg.HeadType.MESSAGE_NOTICE);
		protoMsgBuilder.setMessageNotice(messageNotice);
		return protoMsgBuilder.build();
	}
}
