package com.github.fishlikewater.httppierce.client;


import com.github.fishlikewater.httppierce.codec.MessageCodec;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.handler.ClientHeartBeatHandler;
import com.github.fishlikewater.httppierce.handler.ClientMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author fishlikewater@126.com
 * @since 2018年12月25日 15:05
 **/
public class ClientHandlerInitializer extends ChannelInitializer<Channel> {

    private final HttpPierceClientConfig httpPierceClientConfig;
    private final ClientBoot clientBoot;


    public ClientHandlerInitializer(HttpPierceClientConfig httpPierceClientConfig, ClientBoot clientBoot) {
        this.httpPierceClientConfig = httpPierceClientConfig;
        this.clientBoot = clientBoot;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        /* open log*/
        if (httpPierceClientConfig.isLogger()) {
            pipeline.addLast(new LoggingHandler());
        }
        pipeline
                .addLast(new LengthFieldBasedFrameDecoder((int) httpPierceClientConfig.getMaxFrameLength().toBytes(), 0, 4))
                .addLast(new MessageCodec())
                .addLast(new IdleStateHandler(0, 0, httpPierceClientConfig.getTimeout().getSeconds(), TimeUnit.SECONDS))
                .addLast(new ClientHeartBeatHandler())
                .addLast(new ClientMessageHandler(httpPierceClientConfig, clientBoot));

    }
}
