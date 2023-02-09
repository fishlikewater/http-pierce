package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.DataMessage;
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
public class MessageTransferHandler extends SimpleChannelInboundHandler<DataMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataMessage msg) throws Exception {

    }
}
