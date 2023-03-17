package com.github.fishlikewater.httppierce.server;


import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月21日 11:19
 **/
@Slf4j
public class DynamicHttpBoot extends HttpBoot{

    @Getter
    private final int port;
    private final String registerName;
    private final Channel channel;
    private final HttpPierceServerConfig httpPierceServerConfig;


    public DynamicHttpBoot(int port, String registerName, Channel channel, HttpPierceServerConfig httpPierceServerConfig){
        this.port = port;
        this.registerName = registerName;
        this.channel = channel;
        this.httpPierceServerConfig = httpPierceServerConfig;
    }


    @Override
    public void run(ServerBootstrap serverBootstrap) {

        serverBootstrap.childHandler(new DynamicHttpHandlerInitializer(channel, registerName, httpPierceServerConfig));
        try {
            Channel ch = serverBootstrap.bind(port).sync().channel();
            log.info("⬢ start dynamic http server this port:{}", port);
            ch.closeFuture().addListener(t -> log.info("⬢ dynamic http server【{}】 closed", port));
        } catch (InterruptedException e) {
            log.error("⬢ start dynamic http server fail", e);
        }
    }


    @Override
    public void stop() {
        log.info("⬢ dynamic http server【{}】 shutdown ...", port);
        try {
            if (super.getBossGroup() != null) {
                super.getBossGroup().shutdownGracefully().sync();
            }
            if (super.getWorkerGroup() != null) {
                super.getWorkerGroup().shutdownGracefully().sync();
            }
        } catch (Exception e) {
            log.error("⬢ dynamic http server【{}】 shutdown error", port, e);
        }
    }
}
