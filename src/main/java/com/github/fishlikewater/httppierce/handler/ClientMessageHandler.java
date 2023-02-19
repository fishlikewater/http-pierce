package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.Message;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.kit.CacheUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 客户端消息处理处理器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月17日 15:06
 **/
@Slf4j
@RequiredArgsConstructor
public class ClientMessageHandler extends SimpleChannelInboundHandler<Message> {

    private final HttpPierceClientConfig httpPierceClientConfig;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof SysMessage sysMessage) {
            switch (sysMessage.getCommand()) {
                case AUTH -> {
                    final int state = sysMessage.getState();
                    if (state == 1) {
                        /*  Verification successful, start registering service*/
                        final Map<String, HttpPierceClientConfig.HttpMapping> mappingMap = ctx.channel().attr(CacheUtil.CLIENT_FORWARD).get();
                        mappingMap.forEach((k, v) -> {
                            final SysMessage registerMsg = new SysMessage();
                            registerMsg.setCommand(Command.REGISTER)
                                    .setId(IdUtil.getSnowflakeNextId())
                                    .setRegister(SysMessage.Register.builder()
                                            .registerName(k)
                                            .newServerPort(v.isNewServerPort())
                                            .newPort(v.getNewPort())
                                            .build());
                            ctx.writeAndFlush(registerMsg);
                        });


                    } else {
                        log.error("Token verification failed. Please check the configuration");
                    }
                }
                case HEALTH -> {
                    log.debug("Heartbeat packet received");
                }
                default -> {
                }
            }
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(CacheUtil.CLIENT_FORWARD).set(new ConcurrentHashMap<>());
        final HttpPierceClientConfig.HttpMapping[] httpMappings = httpPierceClientConfig.getHttpMappings();
        for (HttpPierceClientConfig.HttpMapping httpMapping : httpMappings) {
            ctx.channel().attr(CacheUtil.CLIENT_FORWARD).get().put(httpMapping.getRegisterName(), httpMapping);
        }
        final SysMessage sysMessage = new SysMessage();
        sysMessage.setCommand(Command.AUTH)
                .setToken(httpPierceClientConfig.getToken())
                .setId(IdUtil.getSnowflakeNextId());
        ctx.channel().writeAndFlush(sysMessage);

    }
}
