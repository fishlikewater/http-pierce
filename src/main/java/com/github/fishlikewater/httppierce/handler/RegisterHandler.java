package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.ObjectUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.config.Constant;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.config.ProtocolEnum;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.server.DynamicHttpBoot;
import com.github.fishlikewater.httppierce.server.DynamicTcpBoot;
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
 * 注册处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:37
 **/
@RequiredArgsConstructor
public class RegisterHandler extends SimpleChannelInboundHandler<SysMessage> {

    private final HttpPierceServerConfig httpPierceServerConfig;
    private final HttpPierceConfig httpPierceConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SysMessage msg) {
        final Command command = msg.getCommand();
        if (command == Command.REGISTER) {
            handlerRegister(ctx, msg);

        } else if (command == Command.CANCEL_REGISTER) {
            handlerCancelRegister(ctx, msg);
        }
        ctx.fireChannelRead(msg);
    }

    private static void handlerCancelRegister(ChannelHandlerContext ctx, SysMessage msg) {
        final String registerName = msg.getRegister().getRegisterName();
        final Channel channel = ChannelUtil.ROUTE_MAPPING.get(registerName);
        if (Objects.nonNull(channel)) {
            ChannelUtil.ROUTE_MAPPING.remove(registerName);
        } else {
            final List<DynamicTcpBoot> dynamicTcpBoots = ctx.channel().attr(ChannelUtil.CHANNEL_DYNAMIC_BOOT).get();
            for (DynamicTcpBoot dynamicTcpBoot : dynamicTcpBoots) {
                if (dynamicTcpBoot.getRegisterName().equals(registerName)) {
                    ChannelUtil.DYNAMIC_BOOT.remove("port" + dynamicTcpBoot.getPort());
                    dynamicTcpBoot.stop();
                    break;
                }
            }
        }
        ctx.channel().writeAndFlush(msg);
    }

    private void handlerRegister(ChannelHandlerContext ctx, SysMessage msg) {
        final SysMessage.Register register = msg.getRegister();
        final boolean newServerPort = register.isNewServerPort();
        if (newServerPort) {
            registerNewPort(ctx, msg);
        } else {
            registerDefaultPort(ctx, msg);
        }
    }

    private void registerDefaultPort(ChannelHandlerContext ctx, SysMessage msg) {
        SysMessage.Register register = msg.getRegister();
        final SysMessage returnMsg = new SysMessage();
        returnMsg.setCommand(Command.REGISTER);
        returnMsg.setId(msg.getId());
        final String registerName = register.getRegisterName();
        final Channel channel = ChannelUtil.ROUTE_MAPPING.get(registerName);
        if (Objects.nonNull(channel)) {
            returnMsg.setState(Constant.INT_ZERO);
            if (!channel.isActive() || !channel.isWritable()) {
                channel.close();
            }
        } else {
            ChannelUtil.ROUTE_MAPPING.put(registerName, ctx.channel());
            returnMsg.setState(Constant.INT_ONE);
            register.setNewPort(httpPierceServerConfig.getHttpServerPort());
            ctx.channel().attr(ChannelUtil.REGISTER_CHANNEL).get().add(registerName);
        }
        returnMsg.setRegister(register);
        ctx.channel().writeAndFlush(returnMsg);
    }

    private void registerNewPort(ChannelHandlerContext ctx, SysMessage msg) {
        SysMessage.Register register = msg.getRegister();
        final Map<String, DynamicTcpBoot> dynamicHttpBootMap = ChannelUtil.DYNAMIC_BOOT;
        final DynamicTcpBoot dynamicHttpBoot1 = dynamicHttpBootMap.get("port" + register.getNewPort());
        final SysMessage returnMsg = new SysMessage();
        returnMsg.setCommand(Command.REGISTER);
        returnMsg.setId(msg.getId());
        returnMsg.setRegister(register);
        if (ObjectUtil.isNull(dynamicHttpBoot1)) {
            if (register.getProtocol() == ProtocolEnum.TCP) {
                final DynamicTcpBoot dynamicTcpBoot = new DynamicTcpBoot(register.getNewPort(), register.getRegisterName(), ctx.channel());
                dynamicTcpBoot.start();
                dynamicHttpBootMap.put("port" + register.getNewPort(), dynamicTcpBoot);
                ctx.channel().attr(ChannelUtil.CHANNEL_DYNAMIC_BOOT).get().add(dynamicTcpBoot);
            } else {
                final DynamicHttpBoot dynamicHttpBoot = new DynamicHttpBoot(register.getNewPort(), register.getRegisterName(),
                        ctx.channel(), httpPierceServerConfig, httpPierceConfig, register.getProtocol());
                dynamicHttpBoot.start();
                dynamicHttpBootMap.put("port" + register.getNewPort(), dynamicHttpBoot);
                ctx.channel().attr(ChannelUtil.CHANNEL_DYNAMIC_BOOT).get().add(dynamicHttpBoot);
            }
            returnMsg.setState(Constant.INT_ONE);
        } else {
            if (!dynamicHttpBoot1.getChannel().isActive() || dynamicHttpBoot1.getChannel().isWritable()) {
                ChannelUtil.DYNAMIC_BOOT.remove("port" + dynamicHttpBoot1.getPort());
                dynamicHttpBoot1.stop();
                dynamicHttpBoot1.getChannel().close();
            }
            returnMsg.setState(Constant.INT_TWO);
        }
        ctx.channel().writeAndFlush(returnMsg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(ChannelUtil.REGISTER_CHANNEL).set(new ArrayList<>());
        ctx.channel().attr(ChannelUtil.CHANNEL_DYNAMIC_BOOT).set(new ArrayList<>());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final List<String> list = ctx.channel().attr(ChannelUtil.REGISTER_CHANNEL).get();
        list.forEach(ChannelUtil.ROUTE_MAPPING::remove);
        final List<DynamicTcpBoot> dynamicTcpBoots = ctx.channel().attr(ChannelUtil.CHANNEL_DYNAMIC_BOOT).get();
        dynamicTcpBoots.forEach(dynamicHttpBoot -> {
            ChannelUtil.DYNAMIC_BOOT.remove("port" + dynamicHttpBoot.getPort());
            dynamicHttpBoot.stop();
        });
        super.channelInactive(ctx);
    }
}
