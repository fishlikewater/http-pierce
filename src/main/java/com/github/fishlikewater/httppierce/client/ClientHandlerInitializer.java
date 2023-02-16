package com.github.fishlikewater.httppierce.client;


import com.github.fishlikewater.httppierce.codec.MessageCodec;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.handler.ClientHeartBeatHandler;
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

    private final HttpPierceConfig httpPierceConfig;


    public ClientHandlerInitializer(HttpPierceConfig httpPierceConfig) {
        this.httpPierceConfig = httpPierceConfig;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        /* 是否打开日志*/
        if (httpPierceConfig.isLogger()) {
            pipeline.addLast(new LoggingHandler());
        }
        pipeline
                .addLast(new LengthFieldBasedFrameDecoder(5*1024 * 1024, 0, 4))
                .addLast(new MessageCodec())
                .addLast(new IdleStateHandler(0, 0, httpPierceConfig.getTimeout(), TimeUnit.SECONDS))
                .addLast(new ClientHeartBeatHandler());

    }
}
