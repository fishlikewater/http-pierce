package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        Long requestId = IdUtil.getSnowflakeNextId();
        ChannelUtil.REQUEST_MAPPING.put(requestId, channel);
        final DataMessage dataMessage = new DataMessage();
        dataMessage.setCommand(Command.REQUEST);
        dataMessage.setDstServer(registerName);
        dataMessage.setBytes(msg);
        dataMessage.setId(requestId);
        ctx.channel().attr(ChannelUtil.TCP_CHANNEL).get().add(requestId);
        channel.writeAndFlush(dataMessage).addListener((f) -> {
            if (f.isSuccess()) {
                ChannelUtil.TIMED_CACHE.put(requestId, ctx.channel());
            } else {
                log.info("Forwarding failed");
            }

        });
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(ChannelUtil.TCP_CHANNEL).set(new ArrayList<>());
        super.handlerAdded(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final List<Long> list = ctx.channel().attr(ChannelUtil.TCP_CHANNEL).get();
        list.forEach(id -> {
            ChannelUtil.REQUEST_MAPPING.remove(id);
            ChannelUtil.TIMED_CACHE.remove(id);
        });
        list.clear();
        ctx.channel().attr(ChannelUtil.TCP_CHANNEL).set(null);
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
