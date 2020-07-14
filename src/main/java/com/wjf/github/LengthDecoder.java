package com.wjf.github;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;

import java.util.List;

public class LengthDecoder extends ByteToMessageDecoder {

	private static final Logger log = Logger.getLogger(LengthDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < (NettyProperties.head_length)) {
			return;
		}
		int i = in.getInt(NettyProperties.length_offset);

		if (in.readableBytes() < i + NettyProperties.head_length) {
			return;
		}
		byte[] bytes = new byte[i];
		log.info("消息体长度：" + i);
		in.getBytes(in.readerIndex() + NettyProperties.head_length, bytes);
		//在解码完成过后必须在输入的ByteBuf中读出对应字节数量的信息
		ByteBuf buffer = Unpooled.buffer(i);
		buffer.writeBytes(bytes);
		in.readBytes(i + NettyProperties.head_length);
		out.add(buffer);
	}
}
