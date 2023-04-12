package com.github.fishlikewater.httppierce.server;

import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.handler.DynamicHttpServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  http 服务端 处理器初始化
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:41
 **/
@Slf4j
@RequiredArgsConstructor
public class DynamicHttpHandlerInitializer extends ChannelInitializer<Channel> {

    private final Channel clientChannel;

    private final String registerName;

    private final HttpPierceServerConfig httpPierceServerConfig;

    private final HttpPierceConfig httpPierceConfig;

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        p.addLast("httpCode", new HttpRequestDecoder());
        p.addLast(new ChunkedWriteHandler());
        p.addLast("aggregator", new HttpObjectAggregator((int)httpPierceServerConfig.getHttpObjectSize().toBytes()));
        p.addLast("byte", new ByteArrayEncoder());
        p.addLast("httpServerHandler", new DynamicHttpServerHandler(clientChannel, registerName, httpPierceConfig));
    }
}
