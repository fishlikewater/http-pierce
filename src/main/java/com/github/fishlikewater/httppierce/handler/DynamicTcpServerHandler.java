package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.kit.IdUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

/**
 * <p>
 * http 服务器处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:34
 **/
@Slf4j
@RequiredArgsConstructor
public class DynamicTcpServerHandler extends SimpleChannelInboundHandler<byte[]> {

    private final Channel channel;

    private final String registerName;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) {

        Long requestId = ctx.channel().attr(ChannelUtil.TCP_FLAG).get();
        if (Objects.isNull(requestId)) {
            requestId = IdUtil.generateId();
            ctx.channel().attr(ChannelUtil.TCP_FLAG).set(requestId);
        }
        ChannelUtil.REQUEST_MAPPING.put(requestId, ctx.channel());
        final DataMessage dataMessage = new DataMessage();
        dataMessage.setCommand(Command.REQUEST);
        dataMessage.setDstServer(registerName);
        dataMessage.setBytes(msg);
        dataMessage.setId(requestId);
        channel.writeAndFlush(dataMessage);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long requestId = ctx.channel().attr(ChannelUtil.TCP_FLAG).get();
        if (Objects.nonNull(requestId)) {
            ChannelUtil.REQUEST_MAPPING.remove(requestId);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // The remote host forced to close an existing connection
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
