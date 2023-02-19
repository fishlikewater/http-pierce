package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.kit.CacheUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
    protected void channelRead0(ChannelHandlerContext ctx, SysMessage msg) {
        final Command command = msg.getCommand();
        if (command == Command.REGISTER) {
            final SysMessage.Register register = msg.getRegister();
            final boolean newServerPort = register.isNewServerPort();
            if (newServerPort){

            }else {
                final SysMessage returnMsg = new SysMessage();
                returnMsg.setCommand(Command.REGISTER);
                returnMsg.setId(IdUtil.getSnowflakeNextId());
                returnMsg.setRegister(register);
                final String registerName = register.getRegisterName();
                final Channel channel = ctx.channel().attr(CacheUtil.SERVER_FORWARD).get().get(registerName);
                if (Objects.nonNull(channel)){
                    returnMsg.setState(0);
                }else {
                    ctx.channel().attr(CacheUtil.SERVER_FORWARD).get().put(registerName, ctx.channel());
                    returnMsg.setState(1);
                }
                ctx.channel().writeAndFlush(returnMsg);
            }

        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(CacheUtil.SERVER_FORWARD).set(new ConcurrentHashMap<>());
        super.channelActive(ctx);
    }
}
