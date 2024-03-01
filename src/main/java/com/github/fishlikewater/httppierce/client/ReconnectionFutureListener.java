package com.github.fishlikewater.httppierce.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 自定义重连处理
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月16日 19:25
 **/
@Slf4j
public record ReconnectionFutureListener(ClientBoot clientBoot) implements ChannelFutureListener {

    @Override
    public void operationComplete(ChannelFuture future) {
        if (future.isSuccess()) {
            log.info("Connection successful...");
        } else {
            log.info("Connection failed, will initiate reconnection after 30s");
            final EventLoop loop = future.channel().eventLoop();
            loop.schedule(clientBoot::connection, clientBoot.getHttpPierceClientConfig().getRetryTime().toSeconds(), TimeUnit.SECONDS);
        }
    }
}
