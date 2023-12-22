package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 验证处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:36
 **/
@RequiredArgsConstructor
public class AuthHandler extends SimpleChannelInboundHandler<SysMessage> {

    private final String token;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SysMessage msg) {
        final Command command = msg.getCommand();
        if (command == Command.AUTH) {
            final String tokenStr = msg.getToken();
            if (token.equals(tokenStr)) {
                final SysMessage successMsg = new SysMessage();
                successMsg
                        .setCommand(Command.AUTH)
                        .setState(1)
                        .setId(IdUtil.getSnowflakeNextId());
                ctx.writeAndFlush(successMsg);
                ctx.pipeline().remove(this);
                return;
            }

        }
        final SysMessage failMsg = new SysMessage();
        failMsg
                .setCommand(Command.AUTH)
                .setState(0)
                .setId(IdUtil.getSnowflakeNextId());
        ctx.writeAndFlush(failMsg);
    }
}
