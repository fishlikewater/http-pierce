package com.github.fishlikewater.httppierce.server;

import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.kit.BootStrapFactory;
import com.github.fishlikewater.httppierce.kit.EpollKit;
import com.github.fishlikewater.httppierce.kit.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务端启动器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:41
 **/
@Slf4j
@RequiredArgsConstructor
public class ServerBoot implements Boot {

    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    /**
     * 处理连接后的channel
     */
    private EventLoopGroup workerGroup;

    private final HttpPierceServerConfig httpPierceServerConfig;

    private final HttpPierceConfig httpPierceConfig;

    @Override
    public void start() {

        final ServerBootstrap serverBootstrap = BootStrapFactory.getServerBootstrap();
        if (EpollKit.epollIsAvailable()) {
            bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-transfer-boss@"));
            workerGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-transfer-worker@"));
            serverBootstrap.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-transfer-boss@"));
            workerGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-transfer-worker@"));
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
        }
        serverBootstrap.childHandler(new ServerHandlerInitializer(httpPierceServerConfig, httpPierceConfig));
        try {
            Channel ch = serverBootstrap.bind(httpPierceServerConfig.getAddress(), httpPierceServerConfig.getTransferPort()).sync().channel();
            log.info("⬢ start transfer server this port:{} and address:{}", httpPierceServerConfig.getTransferPort(), httpPierceServerConfig.getAddress());
            ch.closeFuture().addListener(t -> log.info("⬢  transfer server closed"));
        } catch (InterruptedException e) {
            log.error("⬢ start transfer server fail", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 关闭服务
     */
    @Override
    public void stop() {
        log.info("⬢ transfer server shutdown ...");
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            if (this.workerGroup != null) {
                this.workerGroup.shutdownGracefully().sync();
            }
        } catch (InterruptedException e) {
            log.error("⬢ transfer server shutdown error", e);
            Thread.currentThread().interrupt();
        }
    }

}
