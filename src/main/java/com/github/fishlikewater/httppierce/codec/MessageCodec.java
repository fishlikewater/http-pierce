package com.github.fishlikewater.httppierce.codec;

import cn.hutool.core.util.ObjectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.StandardCharsets;
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
        //占位
        out.writeInt(0);
        if (msg instanceof DataMessage){
            //写入数据类型
            out.writeByte(DATA_MSG);
            final byte[] bytes = ObjectUtil.serialize(msg);
            out.writeBytes(bytes);
            final int length = out.readableBytes();
            out.setInt(0, length-4);
        }
        if (msg instanceof final SysMessage sysMessage){
            //写入数据类型
            out.writeByte(SYS_MSG);
            //写入消息id
            out.writeLong(sysMessage.getId());
            //写入消息类型
            out.writeInt(sysMessage.getCommand().getCode());
            out.writeInt(sysMessage.getState());
            //验证时 需要写入token
            byte[] bytes = new byte[0];
            if (sysMessage.getCommand() == Command.AUTH){
                final String token = sysMessage.getToken();
                bytes = token.getBytes(StandardCharsets.UTF_8);

            }
            if (sysMessage.getCommand() == Command.REGISTER){
                final String registerNames = sysMessage.getRegisterNames();
                bytes = registerNames.getBytes(StandardCharsets.UTF_8);
            }
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
            final int length = out.readableBytes();
            out.setInt(0, length-4);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.isReadable()){
            in.readInt();
            final byte msgType = in.readByte();
            if (msgType == SYS_MSG){
                final SysMessage sysMessage = new SysMessage();
                final long msgId = in.readLong();
                final int command = in.readInt();
                final int state = in.readInt();

                final int length = in.readInt();
                final byte[] bytes = new byte[length];
                in.readBytes(bytes);
                final String msg = new String(bytes, StandardCharsets.UTF_8);
                if (command == Command.AUTH.getCode()){
                    sysMessage.setCommand(Command.AUTH);
                    sysMessage.setToken(msg);
                }
                if (command == Command.REGISTER.getCode()){
                    sysMessage.setCommand(Command.REGISTER);
                    sysMessage.setRegisterNames(msg);
                }
                sysMessage.setId(msgId);
                sysMessage.setState(state);
                out.add(sysMessage);
            }
            if (msgType == DATA_MSG){
                final int readableBytes = in.readableBytes();
                final byte[] bytes = new byte[readableBytes];
                in.readBytes(bytes);
                out.add(ObjectUtil.deserialize(bytes));
            }
        }
    }

}
