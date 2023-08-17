package com.github.fishlikewater.httppierce.server;

import cn.hutool.core.io.FileUtil;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.handler.HttpHeartBeatHandler;
import com.github.fishlikewater.httppierce.handler.HttpServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.File;
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
public class HttpHandlerInitializer extends ChannelInitializer<Channel> {

    private final HttpPierceServerConfig httpPierceServerConfig;
    private final HttpPierceConfig httpPierceConfig;

    public HttpHandlerInitializer(HttpPierceServerConfig httpPierceServerConfig,  HttpPierceConfig httpPierceConfig) {
        log.info("init http handler");
        this.httpPierceServerConfig = httpPierceServerConfig;
        this.httpPierceConfig = httpPierceConfig;
    }

    @Override
    protected void initChannel(Channel channel) throws SSLException {
        ChannelPipeline p = channel.pipeline();
        p.addLast(new IdleStateHandler(0, 0, httpPierceServerConfig.getTimeout().getSeconds(), TimeUnit.SECONDS));
        p.addLast(new HttpHeartBeatHandler());
        /* open log ?*/
        if (httpPierceServerConfig.isLogger()) {
            p.addLast(new LoggingHandler());
        }
        p.addLast("httpCode", new HttpRequestDecoder());
        p.addLast(new ChunkedWriteHandler());
        p.addLast("aggregator", new HttpObjectAggregator((int) httpPierceServerConfig.getHttpObjectSize().toBytes()));
        p.addLast("byte", new ByteArrayEncoder());
        p.addLast("httpServerHandler", new HttpServerHandler(httpPierceConfig));
        if(httpPierceServerConfig.getSslConfig().isEnable()){
            final File keyFile = FileUtil.file(httpPierceServerConfig.getSslConfig().getPkPath());
            final File cerFile = FileUtil.file(httpPierceServerConfig.getSslConfig().getCaPath());
            final SslContext sslContext = SslContextBuilder.forServer(cerFile, keyFile).build();
            p.addFirst(sslContext.newHandler(channel.alloc()));
        }
    }
}
