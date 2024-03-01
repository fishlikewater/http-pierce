package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

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
    private final Channel channel;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bytes) {
        final DataMessage dataMessage = new DataMessage();
        dataMessage.setCommand(Command.REQUEST);
        dataMessage.setBytes(bytes);
        dataMessage.setId(requestId);
        channel.writeAndFlush(dataMessage);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtil.REQUEST_MAPPING.remove(requestId);
        super.channelInactive(ctx);
    }
}
