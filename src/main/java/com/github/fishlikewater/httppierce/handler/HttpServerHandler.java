package com.github.fishlikewater.httppierce.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;

/**
 * <p>
 *  http 服务器处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:34
 **/
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

    }
}
