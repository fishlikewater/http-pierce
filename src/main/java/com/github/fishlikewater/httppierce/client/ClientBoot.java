package com.github.fishlikewater.httppierce.client;

import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.kit.BootStrapFactory;
import com.github.fishlikewater.httppierce.kit.ClientKit;
import com.github.fishlikewater.httppierce.kit.EpollKit;
import com.github.fishlikewater.httppierce.kit.NamedThreadFactory;
import com.github.fishlikewater.httppierce.server.Boot;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  客户端启动器
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:41
 **/
@Slf4j
@RequiredArgsConstructor
public class ClientBoot implements Boot {

    @Getter
    private final HttpPierceClientConfig httpPierceClientConfig;

    /**
     * 处理连接
     */
    private EventLoopGroup bossGroup;

    private Bootstrap bootstrap;

    @Override
    public void start() {
        bootstrap = BootStrapFactory.bootstrapConfig();
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * 60 * 1000);
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024));
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        if (EpollKit.epollIsAvailable()) {
            bossGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("client-epoll-boss@"));
            bootstrap.group(bossGroup);
        } else {
            bossGroup = new NioEventLoopGroup(0, new NamedThreadFactory("client-nio-boss@"));
            bootstrap.group(bossGroup);
        }
        bootstrap.handler(new ClientHandlerInitializer(httpPierceClientConfig, this));
        connection();
    }

    /**
     * 将连接及其后续操作单独提炼出来，方便重连操作
     * @since 2023/2/16 19:33
     * @author fishlikewater@126.com
     */
    public void connection(){
        try {
            final ChannelFuture channelFuture = bootstrap
                    .connect(httpPierceClientConfig.getServerAddress(), httpPierceClientConfig.getServerPort())
                    .addListener(new ReconnectionFutureListener(this))
                    .sync();
            channelFuture.channel().closeFuture().addListener(t -> log.info("⬢  client server closed"));
            ClientKit.setChannel(channelFuture.channel());
        }catch (Exception e){
            log.error("start client fail", e);
        }

    }

    @Override
    public void stop() {
        log.info("⬢ client shutdown ...");
        try {
            if (this.bossGroup != null) {
                this.bossGroup.shutdownGracefully().sync();
            }
        } catch (Exception e) {
            log.error("⬢ client shutdown error", e);
        }
    }
}
