package com.github.fishlikewater.httppierce.server;

import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.handler.HttpHeartBeatHandler;
import com.github.fishlikewater.httppierce.handler.HttpServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  http 服务端 处理器初始化
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:41
 **/
@Slf4j
public class HttpInitializer extends ChannelInitializer<Channel> {

    private final HttpPierceConfig httpPierceConfig;

    public HttpInitializer(HttpPierceConfig httpPierceConfig) {
        log.info("init http handler");
        this.httpPierceConfig = httpPierceConfig;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        p.addLast(new IdleStateHandler(0, 0, httpPierceConfig.getTimeout(), TimeUnit.SECONDS));
        p.addLast(new HttpHeartBeatHandler());
        /* 是否打开日志*/
        if (httpPierceConfig.isLogger()) {
            p.addLast(new LoggingHandler());
        }
        p.addLast("httpCode", new HttpServerCodec());
        p.addLast(new ChunkedWriteHandler());
        p.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 100));
        p.addLast("httpServerHandler", new HttpServerHandler());
    }
}
