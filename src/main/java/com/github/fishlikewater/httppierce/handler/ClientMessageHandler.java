package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.codec.Message;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.kit.BootStrapFactory;
import com.github.fishlikewater.httppierce.kit.CacheUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
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
    protected void channelRead0(ChannelHandlerContext ctx, Message msg){
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
                case HEALTH -> log.debug("Heartbeat packet received");

                default -> {
                }
            }
        }
        if (msg instanceof DataMessage dataMessage){
            if (dataMessage.getCommand() == Command.REQUEST){
                final String dstServer = dataMessage.getDstServer();
                final HttpPierceClientConfig.HttpMapping httpMapping = ctx.channel().attr(CacheUtil.CLIENT_FORWARD).get().get(dstServer);
                String url = dataMessage.getUrl();
                if (httpMapping.isDelRegisterName()){
                    url = url.replaceAll("/" + httpMapping.getRegisterName(), "");
                }
                FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.valueOf(dataMessage.getVersion()), HttpMethod.valueOf(dataMessage.getMethod()), url);
                dataMessage.getHeads().forEach((k,v)-> req.headers().add(k, v));
                req.headers().set("Host", (httpMapping.getAddress() + ":" + httpMapping.getPort()));
                req.content().writeBytes(dataMessage.getBytes());

                Promise<Channel> promise = BootStrapFactory.createPromise(httpMapping.getAddress(), httpMapping.getPort(), ctx);
                promise.addListener((FutureListener<Channel>) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        ChannelPipeline p = channelFuture.get().pipeline();
                        p.addLast(new ClientResponseHandler(dataMessage.getId(), ctx.channel()));
                        channelFuture.get().writeAndFlush(req);
                    }
                });
            }
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
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
