package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.codec.SysMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <p>
 *  验证处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:36
 **/
public class AuthHandler extends SimpleChannelInboundHandler<SysMessage>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SysMessage msg) throws Exception {

    }
}