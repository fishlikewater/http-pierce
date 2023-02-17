package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.codec.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <p>
 *  消息传输处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:35
 **/
public class MessageTransferHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

    }
}
