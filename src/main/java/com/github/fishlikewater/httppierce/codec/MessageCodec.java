package com.github.fishlikewater.httppierce.codec;

import com.github.fishlikewater.httppierce.kit.KryoUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * <p>
 *  统一消息编解码器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月07日 11:05
 **/
public class MessageCodec extends ByteToMessageCodec<Message> {

    private final static byte SYS_MSG = 0;
    private final static byte DATA_MSG = 1;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        if (out.isWritable()){
            encode(msg, out);
        }
    }

    public void encode(Message msg, ByteBuf out){
        out.writeInt(0);
        if (msg instanceof DataMessage){
            out.writeByte(DATA_MSG);
        }
        if (msg instanceof SysMessage){
            out.writeByte(SYS_MSG);
        }
        final byte[] bytes = KryoUtil.writeObjectToByteArray(msg);
        out.writeBytes(bytes);
        final int length = out.readableBytes();
        out.setInt(0, length-4);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.isReadable()){
            in.readInt();
            final byte msgType = in.readByte();
            final int readableBytes = in.readableBytes();
            final byte[] bytes = new byte[readableBytes];
            in.readBytes(bytes);
            if (msgType == SYS_MSG){
                out.add(KryoUtil.readObjectFromByteArray(bytes, SysMessage.class));
            }
            if (msgType == DATA_MSG){
                out.add(KryoUtil.readObjectFromByteArray(bytes, DataMessage.class));
            }
        }
    }

}
