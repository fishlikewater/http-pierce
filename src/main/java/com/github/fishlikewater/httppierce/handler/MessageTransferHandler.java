package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;

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
    protected void channelRead0(ChannelHandlerContext ctx, DataMessage msg) {

        final Command command = msg.getCommand();
        if (command == Command.RESPONSE){
            final long id = msg.getId();
            final Channel channel = ChannelUtil.TIMED_CACHE.get(id);
            if (Objects.nonNull(channel)){
                final Boolean aBoolean = channel.attr(ChannelUtil.HTTP_UPGRADE).get();
                if (Objects.nonNull(aBoolean) && aBoolean){
                    //协议升级处理
                    channel.pipeline().remove(HttpRequestDecoder.class);
                    channel.pipeline().remove(HttpObjectAggregator.class);
                    channel.pipeline().remove(HttpServerHandler.class);
                    channel.pipeline().addLast(new WebSocketHandler(id));
                    channel.attr(ChannelUtil.HTTP_UPGRADE).set(null);
                }
                channel.writeAndFlush(msg.getBytes());
            }
        }
    }
}
