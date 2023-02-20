package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.codec.Message;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

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

        final Command command = msg.getCommand();
        if (command == Command.RESPONSE){
            final long id = msg.getId();
            final Channel channel = ChannelUtil.TIMED_CACHE.get(id);
            if (Objects.nonNull(channel)){
                channel.writeAndFlush(msg.getBytes());
            }
        }
    }
}
