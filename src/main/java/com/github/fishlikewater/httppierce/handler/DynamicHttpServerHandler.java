package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.config.Constant;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.kit.LoggerUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

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
public class DynamicHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final Channel channel;

    private final String registerName;

    private final HttpPierceConfig httpPierceConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof FullHttpRequest req) {
            Long requestId = IdUtil.getSnowflakeNextId();
            final String connection = req.headers().get(Constant.CONNECTION);
            if (StrUtil.isNotBlank(connection) && connection.contains(Constant.UPGRADE)) {
                HandlerKit.upWebSocket(ctx.channel(), channel, requestId);
            }
            final Map<String, String> heads = new HashMap<>(8);
            final DataMessage dataMessage = new DataMessage();
            dataMessage.setCommand(Command.REQUEST);
            dataMessage.setDstServer(registerName);
            final ByteBuf content = req.content();
            if (content.hasArray()) {
                dataMessage.setBytes(content.array());
            } else {
                byte[] bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
                dataMessage.setBytes(bytes);
            }
            dataMessage.setId(requestId);
            req.headers().forEach(entry -> heads.put(entry.getKey(), entry.getValue()));
            dataMessage.setHeads(heads);
            dataMessage.setMethod(req.method().name());
            dataMessage.setVersion(req.protocolVersion().text());
            dataMessage.setUrl(req.uri());
            ctx.channel().attr(ChannelUtil.TCP_FLAG).set(requestId);
            channel.writeAndFlush(dataMessage).addListener((f) -> {
                if (f.isSuccess()) {
                    if (httpPierceConfig.isLogger()) {
                        LoggerUtil.info(req.uri() + "---->" + channel.remoteAddress().toString());
                    }
                    ChannelUtil.REQUEST_MAPPING.put(requestId, ctx.channel());
                } else {
                    log.info("Forwarding failed");
                }

            });

        } else {
            log.info("not found http or https request, will close this channel");
            ctx.close();
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
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
