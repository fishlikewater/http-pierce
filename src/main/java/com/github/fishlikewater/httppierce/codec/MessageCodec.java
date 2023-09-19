package com.github.fishlikewater.httppierce.codec;

import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.httppierce.kit.KryoUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        if (msg instanceof DataMessage dataMessage){
            out.writeByte(DATA_MSG);
            out.writeLong(dataMessage.getId());
            out.writeByte(dataMessage.getCommand().getCode());
            final String dstServer = dataMessage.getDstServer();
            if (StrUtil.isBlankIfStr(dstServer)){
                out.writeInt(0);
            }else {
                final byte[] dstServerBytes = dstServer.getBytes();
                out.writeInt(dstServerBytes.length);
                out.writeBytes(dstServerBytes);
            }
            final String url = dataMessage.getUrl();
            if (StrUtil.isBlankIfStr(url)){
                out.writeInt(0);
            }else {
                final byte[] urlBytes = url.getBytes();
                out.writeInt(urlBytes.length);
                out.writeBytes(urlBytes);
            }
            final String method = dataMessage.getMethod();
            if (StrUtil.isBlankIfStr(method)){
                out.writeByte(0);
            }else {
                final byte[] methodBytes = method.getBytes();
                out.writeByte(methodBytes.length);
                out.writeBytes(methodBytes);
            }
            final String version = dataMessage.getVersion();
            if (StrUtil.isBlankIfStr(version)){
                out.writeByte(0);
            }else {
                final byte[] versionBytes = version.getBytes();
                out.writeByte(versionBytes.length);
                out.writeBytes(versionBytes);
            }
            final Map<String, String> heads = dataMessage.getHeads();
            if (Objects.nonNull(heads)){

            }else {
                out.writeInt(0);
            }

        }
        if (msg instanceof SysMessage){
            out.writeByte(SYS_MSG);
            final byte[] bytes = KryoUtil.writeObjectToByteArray(msg);
            out.writeBytes(bytes);

        }
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

    public static void main(String[] args) {
        final byte[] bytes = "POST".getBytes();
        System.out.println(bytes.length);
    }

}
