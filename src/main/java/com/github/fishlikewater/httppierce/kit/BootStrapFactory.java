package com.github.fishlikewater.httppierce.kit;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月15日 15:50
 **/
@Slf4j
public class BootStrapFactory {

    private static Bootstrap bootstrap = null;

    public static Bootstrap bootstrapConfig() {
        return bootstrapConfig(new Bootstrap());
    }

    public static Bootstrap bootstrapConfig(ChannelHandlerContext ctx) {
        if (bootstrap != null) {
            return bootstrap;

        }
        bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop().parent());
        bootstrapConfig(bootstrap);
        return bootstrap;
    }

    public static Bootstrap bootstrapConfig(Bootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        if (EpollKit.epollIsAvailable()) {
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }

        return bootstrap;
    }

    public static ServerBootstrap getServerBootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_BACKLOG, 8192);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        return bootstrap;
    }

    public static Promise<Channel> createPromise(String host, int port, ChannelHandlerContext ctx) {
        Bootstrap bootstrap = BootStrapFactory.bootstrapConfig(ctx);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<>() {

            @Override
            protected void initChannel(Channel ch) {

            }
        });
        final Promise<Channel> promise = ctx.executor().newPromise();
        bootstrap.remoteAddress(host, port);
        bootstrap.connect()
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        promise.setSuccess(channelFuture.channel());
                    } else {
                        log.debug("connection fail address {}, port {}", host, port);
                        channelFuture.cancel(true);
                    }
                });
        return promise;
    }
}
