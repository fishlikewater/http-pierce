package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.config.Constant;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  http 服务器处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:34
 **/
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg){
        if (msg instanceof FullHttpRequest req) {
            HttpHeaders headers = req.headers();
            String uri = req.uri();
            if (StrUtil.isBlank(uri) || Constant.URL_SEPARATOR.equals(uri)){
                final FullHttpResponse badResponse = getBadResponse("No routing path, unable to map to client");
                ctx.channel().writeAndFlush(badResponse);
            }
            String path = headers.get(Constant.REQUEST_ROUTE);
            if (StrUtil.isBlank(path)) {
                final String[] split = uri.split("/");
                path = split[1];
            }
            Channel channel = ChannelUtil.ROUTE_MAPPING.get(path);
            if (channel == null) {
                final FullHttpResponse badResponse = getBadResponse("No client connection, please check the url");
                ctx.channel().writeAndFlush(badResponse);
            } else {
                final DataMessage dataMessage = new DataMessage();
                final Map<String, String> heads = new HashMap<>(8);
                Long requestId = IdUtil.getSnowflakeNextId();
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
                req.headers().forEach(entry-> heads.put(entry.getKey(), entry.getValue()));
                dataMessage.setHeads(heads);
                dataMessage.setUrl(uri);
                dataMessage.setMethod(req.method().name());
                dataMessage.setVersion(req.protocolVersion().text());
                ctx.channel().attr(ChannelUtil.HTTP_CHANNEL).set(requestId);
                channel.writeAndFlush(dataMessage).addListener((f) -> {
                    if (f.isSuccess()) {
                        ChannelUtil.TIMED_CACHE.put(requestId, ctx.channel());
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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final Attribute<Long> attr = ctx.channel().attr(ChannelUtil.HTTP_CHANNEL);
        if (attr != null) {
            ChannelUtil.TIMED_CACHE.remove(attr.get());
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

    private FullHttpResponse getBadResponse(String message){
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        resp.content().writeBytes(bytes);
        resp.headers().set("Content-Type", "text/html;charset=UTF-8");
        resp.headers().setInt("Content-Length", resp.content().readableBytes());
        return resp;
    }
}
