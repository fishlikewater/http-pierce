package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年04月25日 15:23
 **/
@RequiredArgsConstructor
public class WebSocketHandler extends SimpleChannelInboundHandler<byte[]> {

    private final Long requestId;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bytes) {

        final Channel channel = ChannelUtil.REQUEST_MAPPING.get(requestId);
        if (Objects.nonNull(channel)){
            final DataMessage dataMessage = new DataMessage();
            dataMessage.setCommand(Command.REQUEST);
            dataMessage.setBytes(bytes);
            dataMessage.setId(requestId);
            channel.writeAndFlush(dataMessage);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final Attribute<Long> attr = ctx.channel().attr(ChannelUtil.HTTP_CHANNEL);
        if (attr != null) {
            ChannelUtil.TIMED_CACHE.remove(attr.get());
        }
        ChannelUtil.REQUEST_MAPPING.remove(requestId);
        super.channelInactive(ctx);
    }
}
