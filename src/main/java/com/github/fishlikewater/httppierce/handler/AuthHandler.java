package com.github.fishlikewater.httppierce.handler;


import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.kit.IdUtil;
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
            if (!token.equals(tokenStr)) {
                return;
            }
            this.sendAuthMsg(1, ctx);
            ctx.pipeline().remove(this);
            return;
        }
        this.sendAuthMsg(0, ctx);
    }

    private void sendAuthMsg(int state, ChannelHandlerContext ctx) {
        final SysMessage successMsg = new SysMessage();
        successMsg
                .setCommand(Command.AUTH)
                .setState(state)
                .setId(IdUtil.generateId());
        ctx.writeAndFlush(successMsg);
    }
}
