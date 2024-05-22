package com.github.fishlikewater.httppierce.handler;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.fishlikewater.httppierce.client.ClientBoot;
import com.github.fishlikewater.httppierce.codec.*;
import com.github.fishlikewater.httppierce.config.Constant;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.config.ProtocolEnum;
import com.github.fishlikewater.httppierce.entity.ConnectionStateInfo;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.github.fishlikewater.httppierce.kit.BootStrapFactory;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.kit.ClientKit;
import com.github.fishlikewater.httppierce.kit.IdUtil;
import com.github.fishlikewater.httppierce.service.ServiceMappingService;
import io.netty.channel.*;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.github.fishlikewater.httppierce.entity.table.ServiceMappingTableDef.SERVICE_MAPPING;


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
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof SysMessage sysMessage) {
            this.handleSysMsg(ctx, sysMessage);
        }
        if (msg instanceof DataMessage dataMessage && (dataMessage.getCommand() == Command.REQUEST)) {
            this.handleDataMsg(ctx, dataMessage);
        }
        // 处理等待中的请求
        this.handleWaitingRequest(msg);
    }

    private void handleDataMsg(ChannelHandlerContext ctx, DataMessage dataMessage) {
        final Channel channel = ChannelUtil.REQUEST_MAPPING.get(dataMessage.getId());
        if (Objects.nonNull(channel)) {
            channel.writeAndFlush(dataMessage.getBytes());
        } else {
            final String dstServer = dataMessage.getDstServer();
            final ServiceMapping serviceMapping = ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).get().get(dstServer);
            if (serviceMapping.getProtocol().equals(ProtocolEnum.TCP.name())) {
                handlerTcp(ctx, dataMessage, serviceMapping);
            } else {
                handlerHttp(ctx, dataMessage, serviceMapping);
            }
        }
    }

    private void handleWaitingRequest(Message msg) {
        ChannelPromise channelPromise = ClientKit.getPROMISE_MAP().remove(msg.getId());
        if (Objects.nonNull(channelPromise)) {
            channelPromise.setSuccess();
        }
    }

    private void handlerTcp(ChannelHandlerContext ctx, DataMessage dataMessage, ServiceMapping serviceMapping) {
        Promise<Channel> promise = BootStrapFactory.createPromise(serviceMapping.getAddress(), serviceMapping.getLocalPort(), ctx);
        promise.addListener((FutureListener<Channel>) channelFuture -> {
            if (channelFuture.isSuccess()) {
                ChannelUtil.REQUEST_MAPPING.put(dataMessage.getId(), channelFuture.get());
                ChannelPipeline p = channelFuture.get().pipeline();
                p.addLast("byte", new ByteArrayCodec());
                p.addLast(new ClientResponseHandler2(dataMessage.getId(), ctx.channel()));
                channelFuture.get().writeAndFlush(dataMessage.getBytes());
            }
        });
    }

    private void handleSysMsg(ChannelHandlerContext ctx, SysMessage sysMessage) {
        final SysMessage.Register register = sysMessage.getRegister();
        switch (sysMessage.getCommand()) {
            case AUTH -> this.handleAuth(ctx, sysMessage);
            case REGISTER -> this.handleRegister(ctx, sysMessage, register);
            case HEALTH -> log.debug("Heartbeat packet received");
            case CANCEL_REGISTER -> this.handleCancelRegister(ctx, register);
            default -> log.warn("Unknown command received");
        }
    }

    private void handleCancelRegister(ChannelHandlerContext ctx, SysMessage.Register register) {
        log.info("cancel register {}", register.getRegisterName());
        ChannelUtil.stateMap.remove(register.getRegisterName());
        ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).get().remove(register.getRegisterName());
    }

    private void handleRegister(ChannelHandlerContext ctx, SysMessage sysMessage, SysMessage.Register register) {
        final String registerName = register.getRegisterName();
        final ConnectionStateInfo connectionStateInfo = new ConnectionStateInfo();
        connectionStateInfo.setRegisterName(registerName);
        connectionStateInfo.setServicePort(register.getNewPort());
        if (sysMessage.getState() == Constant.INT_ONE) {
            log.info("Successfully registered the route name 【{}】,the url prefix is 【{}】",
                    registerName,
                    httpPierceClientConfig.getServerAddress() + ":" + register.getNewPort());
            connectionStateInfo.setState(Constant.INT_ONE);
            ChannelUtil.stateMap.put(registerName, connectionStateInfo);
        } else if (sysMessage.getState() == Constant.INT_TWO) {
            log.info("Failed to register the route name 【{}】,because Port【{}】  is already in use", registerName, register.getNewPort());
            ctx.channel().eventLoop().schedule(() -> ClientKit.reRegister(registerName), 10, TimeUnit.SECONDS);
            connectionStateInfo.setState(Constant.INT_ZERO);
            ChannelUtil.stateMap.put(registerName, connectionStateInfo);
        } else {
            log.info("Failed to register  the route name 【{}】", registerName);
            ctx.channel().eventLoop().schedule(() -> ClientKit.reRegister(registerName), 10, TimeUnit.SECONDS);
            connectionStateInfo.setState(0);
            ChannelUtil.stateMap.put(registerName, connectionStateInfo);
        }
    }

    private void handleAuth(ChannelHandlerContext ctx, SysMessage sysMessage) {
        final int state = sysMessage.getState();
        if (state == Constant.INT_ONE) {
            /*  Verification successful, start registering service*/
            final Map<String, ServiceMapping> mappingMap = ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).get();
            mappingMap.forEach((k, v) -> ClientKit.registerService(v, false));
        } else {
            log.error("Token verification failed. Please check the configuration");
        }
    }

    private static void handlerHttp(ChannelHandlerContext ctx, DataMessage dataMessage, ServiceMapping serviceMapping) {
        String url = dataMessage.getUrl();
        if (serviceMapping.getDelRegisterName() == Constant.INT_ONE) {
            url = url.replaceAll(Constant.URL_SEPARATOR + serviceMapping.getRegisterName(), "");
        }
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.valueOf(dataMessage.getVersion()), HttpMethod.valueOf(dataMessage.getMethod()), url);
        dataMessage.getHeads().forEach((k, v) -> req.headers().add(k, v));
        req.headers().set("Host", (serviceMapping.getAddress() + ":" + serviceMapping.getLocalPort()));
        req.content().writeBytes(dataMessage.getBytes());
        final String upgrade = req.headers().get(Constant.UPGRADE);
        Promise<Channel> promise = BootStrapFactory.createPromise(serviceMapping.getAddress(), serviceMapping.getLocalPort(), ctx);
        promise.addListener((FutureListener<Channel>) channelFuture -> {
            if (channelFuture.isSuccess()) {
                if (CharSequenceUtil.isNotBlank(upgrade)) {
                    channelFuture.get().attr(ChannelUtil.HTTP_UPGRADE).set(true);
                }
                ChannelUtil.REQUEST_MAPPING.put(dataMessage.getId(), channelFuture.get());
                ChannelPipeline p = channelFuture.get().pipeline();
                p.addLast("http", new HttpRequestEncoder());
                p.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 10));
                p.addLast("byte", new ByteArrayDecoder());
                p.addLast(new ClientResponseHandler(dataMessage.getId(), ctx.channel()));
                channelFuture.get().writeAndFlush(req);
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).set(new ConcurrentHashMap<>(16));

        final ServiceMappingService mappingService = SpringUtil.getBean(ServiceMappingService.class);
        final List<ServiceMapping> list = mappingService.list(mappingService.queryChain().from(SERVICE_MAPPING).where(SERVICE_MAPPING.ENABLE.eq(1)));
        for (ServiceMapping serviceMapping : list) {
            ctx.channel().attr(ChannelUtil.CLIENT_FORWARD).get().put(serviceMapping.getRegisterName(), serviceMapping);
        }
        final SysMessage sysMessage = new SysMessage();
        sysMessage.setCommand(Command.AUTH)
                .setToken(httpPierceClientConfig.getToken())
                .setId(IdUtil.generateId());
        ctx.channel().writeAndFlush(sysMessage);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final EventLoop loop = ctx.channel().eventLoop();
        ChannelUtil.stateMap.clear();
        loop.schedule(clientBoot::connection, httpPierceClientConfig.getRetryTime().getSeconds(), TimeUnit.SECONDS);
    }
}
