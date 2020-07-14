package com.wjf.github;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class LengthEncoder extends MessageToByteEncoder<ProtoMsg.MyProtoMsg> {
	@Override
	protected void encode(ChannelHandlerContext ctx, ProtoMsg.MyProtoMsg msg, ByteBuf out) throws Exception {
		byte[] bytes = msg.toByteArray();
		int length = bytes.length;
		ByteBuf buf = Unpooled.buffer(NettyProperties.head_length + length);
		buf.writeShort(NettyProperties.head_version);
		buf.writeInt(length);
		buf.writeBytes(bytes);
		out.writeBytes(buf);
	}
}
