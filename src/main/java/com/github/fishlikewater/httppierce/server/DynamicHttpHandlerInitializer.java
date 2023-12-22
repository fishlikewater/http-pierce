package com.github.fishlikewater.httppierce.server;

import cn.hutool.core.io.FileUtil;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.config.ProtocolEnum;
import com.github.fishlikewater.httppierce.handler.DynamicHttpServerHandler;
import com.github.fishlikewater.httppierce.kit.SslUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.File;

/**
 * <p>
 * http 服务端 处理器初始化
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

    private final ProtocolEnum protocolEnum;

    @Override
    protected void initChannel(Channel channel) throws SSLException {
        ChannelPipeline p = channel.pipeline();
        if (protocolEnum == ProtocolEnum.https) {
            p.addLast("ssl", SslUtil.getSslContext().newHandler(channel.alloc()));
        }
        p.addLast("httpCode", new HttpRequestDecoder());
        p.addLast(new ChunkedWriteHandler());
        p.addLast("aggregator", new HttpObjectAggregator((int) httpPierceServerConfig.getHttpObjectSize().toBytes()));
        p.addLast("byte", new ByteArrayEncoder());
        p.addLast("httpServerHandler", new DynamicHttpServerHandler(clientChannel, registerName, httpPierceConfig));
        if (httpPierceServerConfig.getSslConfig().isEnable()) {
            final File cerFile = FileUtil.file(httpPierceServerConfig.getSslConfig().getCaPath());
            final File keyFile = FileUtil.file(httpPierceServerConfig.getSslConfig().getPkPath());
            final SslContext sslContext = SslContextBuilder.forServer(cerFile, keyFile).build();
            p.addFirst(sslContext.newHandler(channel.alloc()));
        }
    }
}
