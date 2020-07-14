package com.wjf.github;

import com.google.protobuf.Parser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ProtobufDecoder extends MessageToMessageDecoder<ByteBuf> {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		int i = msg.readableBytes();
		if (i > 0) {
			byte[] bytes = new byte[i];
			msg.getBytes(msg.readerIndex(), bytes);
			Parser<ProtoMsg.MyProtoMsg> parser = ProtoMsg.MyProtoMsg.parser();
			ProtoMsg.MyProtoMsg myProtoMsg = parser.parseFrom(bytes);
			out.add(myProtoMsg);
		}
	}
}
