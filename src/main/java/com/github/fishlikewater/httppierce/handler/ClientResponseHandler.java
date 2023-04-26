package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

/**
 * 客户端处理实际服务器响应
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月13日 13:57
 **/
@Slf4j
@RequiredArgsConstructor
public class ClientResponseHandler extends SimpleChannelInboundHandler<byte[]> {

    private final Long requested;
    private final Channel channel;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] bytes) {
        final DataMessage dataMessage = new DataMessage();
        dataMessage.setCommand(Command.RESPONSE);
        dataMessage.setBytes(bytes);
        dataMessage.setId(requested);
        channel.writeAndFlush(dataMessage).addListener(t-> {
            log.debug("response message");
            final Boolean aBoolean = channel.attr(ChannelUtil.HTTP_UPGRADE).get();
            if (Objects.nonNull(aBoolean) && aBoolean){
                channel.pipeline().remove(HttpRequestEncoder.class);
                channel.pipeline().remove(HttpObjectAggregator.class);
                channel.attr(ChannelUtil.HTTP_UPGRADE).set(null);
            }
        });

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtil.REQUEST_MAPPING.remove(requested);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("happen error: ", cause);
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ChannelUtil.REQUEST_MAPPING.remove(requested);
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
