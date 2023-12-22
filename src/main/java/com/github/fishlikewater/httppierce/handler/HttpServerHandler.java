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
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
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
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final HttpPierceConfig httpPierceConfig;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof FullHttpRequest req) {
            HttpHeaders headers = req.headers();
            String uri = req.uri();
            if (StrUtil.isBlank(uri) || Constant.URL_SEPARATOR.equals(uri)) {
                final ByteBuf buf = getBadResponse("No routing path, unable to map to client");
                ctx.channel().writeAndFlush(buf);
                return;
            }
            String path = headers.get(Constant.REQUEST_ROUTE);
            if (StrUtil.isBlank(path)) {
                final String[] split = uri.split("/");
                path = split[1];
            }
            Long requestId = IdUtil.getSnowflakeNextId();
            Channel channel = ChannelUtil.ROUTE_MAPPING.get(path);
            if (channel == null) {
                final ByteBuf buf = getBadResponse("No client connection, please check the url");
                ctx.channel().writeAndFlush(buf);
            } else {
                final String connection = headers.get(Constant.CONNECTION);
                if (StrUtil.isNotBlank(connection) && connection.contains(Constant.UPGRADE)) {
                    HandlerKit.upWebSocket(ctx.channel(), channel, requestId);
                }
                final DataMessage dataMessage = new DataMessage();
                final Map<String, String> heads = new HashMap<>(8);
                dataMessage.setDstServer(path);
                dataMessage.setCommand(Command.REQUEST);
                dataMessage.setId(requestId);
                final ByteBuf content = req.content();
                if (content.hasArray()) {
                    dataMessage.setBytes(content.array());
                } else {
                    byte[] bytes = new byte[content.readableBytes()];
                    content.readBytes(bytes);
                    dataMessage.setBytes(bytes);
                }
                req.headers().forEach(entry -> heads.put(entry.getKey(), entry.getValue()));
                dataMessage.setHeads(heads);
                dataMessage.setUrl(uri);
                dataMessage.setMethod(req.method().name());
                dataMessage.setVersion(req.protocolVersion().text());
                ctx.channel().attr(ChannelUtil.TCP_FLAG).set(requestId);
                channel.writeAndFlush(dataMessage).addListener((f) -> {
                    if (f.isSuccess()) {
                        ChannelUtil.REQUEST_MAPPING.put(requestId, ctx.channel());
                        if (httpPierceConfig.isLogger()) {
                            LoggerUtil.info(req.uri() + "---->" + channel.remoteAddress().toString());
                        }
                    } else {
                        log.info("Forwarding failed");
                    }

                });
            }
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

    private ByteBuf getBadResponse(String message) {
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        resp.content().writeBytes(bytes);
        resp.headers().set("Content-Type", "text/html;charset=UTF-8");
        resp.headers().setInt("Content-Length", resp.content().readableBytes());
        final EmbeddedChannel embeddedChannel = new EmbeddedChannel(new HttpResponseEncoder());
        embeddedChannel.writeOutbound(resp);
        embeddedChannel.close();
        return embeddedChannel.readOutbound();
    }
}
