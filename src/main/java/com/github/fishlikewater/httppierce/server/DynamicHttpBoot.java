package com.github.fishlikewater.httppierce.server;


import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.config.ProtocolEnum;
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
public class DynamicHttpBoot extends DynamicTcpBoot {

    private final HttpPierceServerConfig httpPierceServerConfig;

    private final HttpPierceConfig httpPierceConfig;

    private final ProtocolEnum protocolEnum;

    public DynamicHttpBoot(int port, String registerName, Channel channel, HttpPierceServerConfig httpPierceServerConfig,
                           HttpPierceConfig httpPierceConfig, ProtocolEnum protocolEnum) {
        super(port, registerName, channel);
        this.httpPierceServerConfig = httpPierceServerConfig;
        this.httpPierceConfig = httpPierceConfig;
        this.protocolEnum = protocolEnum;
    }

    @Override
    public void run(ServerBootstrap serverBootstrap) {
        serverBootstrap.childHandler(new DynamicHttpHandlerInitializer(this.getChannel(), this.getRegisterName(), httpPierceServerConfig, httpPierceConfig, protocolEnum));
        try {
            Channel ch = serverBootstrap.bind(this.getPort()).sync().channel();
            log.info("⬢ start dynamic http server this port:{}", this.getPort());
            ch.closeFuture().addListener(t -> log.info("⬢ dynamic http server【{}】 closed", this.getPort()));
        } catch (InterruptedException e) {
            log.error("⬢ start dynamic http server fail", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        log.info("⬢ dynamic http server【{}】 shutdown ...", this.getPort());
        try {
            if (super.getBossGroup() != null) {
                super.getBossGroup().shutdownGracefully().sync();
            }
            if (super.getWorkerGroup() != null) {
                super.getWorkerGroup().shutdownGracefully().sync();
            }
        } catch (InterruptedException e) {
            log.error("⬢ dynamic http server【{}】 shutdown error", this.getPort(), e);
            Thread.currentThread().interrupt();
        }
    }
}
