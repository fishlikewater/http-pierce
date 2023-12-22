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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * http服务端启动器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:41
 **/
@Getter
@Setter
@Slf4j
@NoArgsConstructor
public class HttpBoot implements Boot {

    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;
    /**
     * 处理连接后的channel
     */
    @Getter
    private EventLoopGroup workerGroup;

    private HttpPierceServerConfig httpPierceServerConfig;

    private HttpPierceConfig httpPierceConfig;

    public HttpBoot(HttpPierceServerConfig httpPierceServerConfig, HttpPierceConfig httpPierceConfig) {
        this.httpPierceServerConfig = httpPierceServerConfig;
        this.httpPierceConfig = httpPierceConfig;
    }


    @Override
    public void start() {

        final ServerBootstrap serverBootstrap = BootStrapFactory.getServerBootstrap();
        if (EpollKit.epollIsAvailable()) {
            bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-http-boss@"));
            workerGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("epoll-http-worker@"));
            serverBootstrap.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-http-boss@"));
            workerGroup = new NioEventLoopGroup(0, new NamedThreadFactory("nio-http-worker@"));
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
        }
        run(serverBootstrap);
    }

    public void run(ServerBootstrap serverBootstrap) {
        serverBootstrap.childHandler(new HttpHandlerInitializer(httpPierceServerConfig, httpPierceConfig));
        try {
            Channel ch = serverBootstrap.bind(httpPierceServerConfig.getAddress(), httpPierceServerConfig.getHttpServerPort()).sync().channel();
            log.info("⬢ start http server this port:{} and address:{}", httpPierceServerConfig.getHttpServerPort(), httpPierceServerConfig.getAddress());
            ch.closeFuture().addListener(t -> log.info("⬢  http server【{}】 closed", httpPierceServerConfig.getHttpServerPort()));
        } catch (InterruptedException e) {
            log.error("⬢ start http server【{}】 fail", httpPierceServerConfig.getHttpServerPort(), e);
        }
    }

    /**
     * 关闭服务
     */
    @Override
    public void stop() {
        log.info("⬢ http server【{}】 shutdown ...", httpPierceServerConfig.getHttpServerPort());
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
            if (this.workerGroup != null) {
                this.workerGroup.shutdownGracefully().sync();
            }
        } catch (Exception e) {
            log.error("⬢ http server【{}】 shutdown error", httpPierceServerConfig.getHttpServerPort(), e);
        }
    }

}
