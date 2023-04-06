package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.server.DynamicHttpBoot;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *  注册处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:37
 **/
@RequiredArgsConstructor
public class RegisterHandler extends SimpleChannelInboundHandler<SysMessage> {

    private final HttpPierceServerConfig httpPierceServerConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SysMessage msg) {
        final Command command = msg.getCommand();
        if (command == Command.REGISTER) {
            final SysMessage.Register register = msg.getRegister();
            final boolean newServerPort = register.isNewServerPort();
            if (newServerPort){
                final Map<String, DynamicHttpBoot> dynamicHttpBootMap = ChannelUtil.DYNAMIC_HTTP_BOOT;
                final DynamicHttpBoot dynamicHttpBoot1 = dynamicHttpBootMap.get("port" + register.getNewPort());
                final SysMessage returnMsg = new SysMessage();
                returnMsg.setCommand(Command.REGISTER);
                returnMsg.setId(IdUtil.getSnowflakeNextId());
                returnMsg.setRegister(register);
                if (ObjectUtil.isNull(dynamicHttpBoot1)){
                    final DynamicHttpBoot dynamicHttpBoot = new DynamicHttpBoot(register.getNewPort(), register.getRegisterName(), ctx.channel(), httpPierceServerConfig);
                    dynamicHttpBoot.start();
                    dynamicHttpBootMap.put("port" + register.getNewPort(), dynamicHttpBoot);
                    ctx.channel().attr(ChannelUtil.CHANNEL_DYNAMIC_HTTP_BOOT).get().add(dynamicHttpBoot);
                    returnMsg.setState(1);
                }else {
                    returnMsg.setState(2);
                }
                ctx.channel().writeAndFlush(returnMsg);
            }else {
                final SysMessage returnMsg = new SysMessage();
                returnMsg.setCommand(Command.REGISTER);
                returnMsg.setId(IdUtil.getSnowflakeNextId());
                returnMsg.setRegister(register);
                final String registerName = register.getRegisterName();
                final Channel channel = ChannelUtil.ROUTE_MAPPING.get(registerName);
                if (Objects.nonNull(channel)){
                    returnMsg.setState(0);
                }else {
                    ChannelUtil.ROUTE_MAPPING.put(registerName, ctx.channel());
                    returnMsg.setState(1);
                    ctx.channel().attr(ChannelUtil.REGISTER_CHANNEL).get().add(registerName);
                }
                ctx.channel().writeAndFlush(returnMsg);
            }

        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(ChannelUtil.REGISTER_CHANNEL).set(new ArrayList<>());
        ctx.channel().attr(ChannelUtil.CHANNEL_DYNAMIC_HTTP_BOOT).set(new ArrayList<>());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final List<String> list = ctx.channel().attr(ChannelUtil.REGISTER_CHANNEL).get();
        list.forEach(ChannelUtil.ROUTE_MAPPING::remove);
        final List<DynamicHttpBoot> dynamicHttpBoots = ctx.channel().attr(ChannelUtil.CHANNEL_DYNAMIC_HTTP_BOOT).get();
        dynamicHttpBoots.forEach(dynamicHttpBoot -> {
            ChannelUtil.DYNAMIC_HTTP_BOOT.remove("port"+dynamicHttpBoot.getPort());
            dynamicHttpBoot.stop();
        });
        super.channelInactive(ctx);
    }
}
