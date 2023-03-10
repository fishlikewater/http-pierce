package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.util.IdUtil;
import com.github.fishlikewater.httppierce.client.ClientBoot;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import com.github.fishlikewater.httppierce.codec.Message;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.kit.BootStrapFactory;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.channel.*;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    private final ClientBoot clientBoot;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg){
        if (msg instanceof SysMessage sysMessage) {
            switch (sysMessage.getCommand()) {
                case AUTH -> {
                    final int state = sysMessage.getState();
                    if (state == 1) {
                        /*  Verification successful, start registering service*/
                        final Map<String, HttpPierceClientConfig.HttpMapping> mappingMap = ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).get();
                        mappingMap.forEach((k, v) -> {
                            final SysMessage registerMsg = new SysMessage();
                            registerMsg.setCommand(Command.REGISTER)
                                    .setId(IdUtil.getSnowflakeNextId())
                                    .setRegister(new SysMessage.Register()
                                            .setRegisterName(k)
                                            .setNewServerPort(v.isNewServerPort())
                                            .setNewPort(v.getNewPort()));
                            ctx.writeAndFlush(registerMsg);
                        });


                    } else {
                        log.error("Token verification failed. Please check the configuration");
                    }
                }
                case REGISTER -> {
                    if (sysMessage.getState() == 1){
                        log.info("Successfully registered the route name 【{}】,the url prefix is 【{}】",
                                sysMessage.getRegister().getRegisterName(),
                                sysMessage.getRegister().isNewServerPort()?httpPierceClientConfig.getServerAddress()+":"+sysMessage.getRegister().getNewPort():
                                        httpPierceClientConfig.getServerAddress()+":[defaultPort]");
                    }else if (sysMessage.getState() == 2){
                        log.info("Failed to register the route name 【{}】,because Port【{}】  is already in use",
                                sysMessage.getRegister().getRegisterName(), sysMessage.getRegister().getNewPort());
                    }else {
                        log.info("Failed to register  the route name 【{}】", sysMessage.getRegister().getRegisterName());
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
                final HttpPierceClientConfig.HttpMapping httpMapping = ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).get().get(dstServer);
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
                        p.addLast("http", new HttpRequestEncoder());
                        p.addLast("aggregator", new HttpObjectAggregator(1024*1024*10));
                        p.addLast("byte", new ByteArrayDecoder());
                        p.addLast(new ClientResponseHandler(dataMessage.getId(), ctx.channel()));
                        channelFuture.get().writeAndFlush(req);
                    }
                });
            }
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).set(new ConcurrentHashMap<>(16));
        final HttpPierceClientConfig.HttpMapping[] httpMappings = httpPierceClientConfig.getHttpMappings();
        for (HttpPierceClientConfig.HttpMapping httpMapping : httpMappings) {
            ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).get().put(httpMapping.getRegisterName(), httpMapping);
        }
        final SysMessage sysMessage = new SysMessage();
        sysMessage.setCommand(Command.AUTH)
                .setToken(httpPierceClientConfig.getToken())
                .setId(IdUtil.getSnowflakeNextId());
        ctx.channel().writeAndFlush(sysMessage);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(clientBoot::connection, 30, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }
}
