package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.SysMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <p>
 *  注册处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:37
 **/
public class RegisterHandler extends SimpleChannelInboundHandler<SysMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SysMessage msg) throws Exception {

    }
}
