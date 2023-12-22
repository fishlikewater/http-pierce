package com.github.fishlikewater.httppierce.server;


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
@Getter
@Slf4j
public class DynamicTcpBoot extends HttpBoot {

    private final int port;
    private final String registerName;
    @Getter
    private final Channel channel;


    public DynamicTcpBoot(int port, String registerName, Channel channel) {
        this.port = port;
        this.registerName = registerName;
        this.channel = channel;
    }


    @Override
    public void run(ServerBootstrap serverBootstrap) {

        serverBootstrap.childHandler(new DynamicTcpHandlerInitializer(channel, registerName));
        try {
            Channel ch = serverBootstrap.bind(port).sync().channel();
            log.info("⬢ start dynamic tcp server this port:{}", port);
            ch.closeFuture().addListener(t -> log.info("⬢ dynamic tcp server【{}】 closed", port));
        } catch (InterruptedException e) {
            log.error("⬢ start dynamic tcp server fail", e);
        }
    }


    @Override
    public void stop() {
        log.info("⬢ dynamic tcp server【{}】 shutdown ...", port);
        try {
            if (super.getBossGroup() != null) {
                super.getBossGroup().shutdownGracefully().sync();
            }
            if (super.getWorkerGroup() != null) {
                super.getWorkerGroup().shutdownGracefully().sync();
            }
        } catch (Exception e) {
            log.error("⬢ dynamic tcp server【{}】 shutdown error", port, e);
        }
    }
}
