package com.github.fishlikewater.httppierce.server;

import com.github.fishlikewater.codec.HttpProtocolCodec;
import com.github.fishlikewater.codec.MyByteToMessageCodec;
import com.github.fishlikewater.config.ProxyType;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.handler.HttpHeartBeatHandler;
import com.github.fishlikewater.server.config.ProxyConfig;
import com.github.fishlikewater.server.handle.ServerHeartBeatHandler;
import com.github.fishlikewater.server.handle.http.*;
import com.github.fishlikewater.server.handle.myprotocol.AuthHandler;
import com.github.fishlikewater.server.handle.myprotocol.MyProtocolHandler;
import com.github.fishlikewater.server.handle.socks.Socks5CommandRequestHandler;
import com.github.fishlikewater.server.handle.socks.Socks5InitialAuthHandler;
import com.github.fishlikewater.server.handle.socks.Socks5PasswordAuthRequestHandler;
import com.github.fishlikewater.server.kit.DefaultConnectionValidate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangx
 * @version V1.0
 * @date: 2019年02月26日 21:47
 **/
@Slf4j
public class ServerInitializer extends ChannelInitializer<Channel> {

    private final HttpPierceConfig httpPierceConfig;

    public ServerInitializer(HttpPierceConfig httpPierceConfig) {
        log.info("init handler");
        this.httpPierceConfig = httpPierceConfig;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        p.addLast(new IdleStateHandler(0, 0, httpPierceConfig.getTimeout(), TimeUnit.SECONDS));
        /* http服务端 无心跳直接关闭*/
        if (httpPierceConfig.getBootType() == HttpPierceConfig.BootType.server || proxyType == ProxyType.http) {
            p.addLast(new HttpHeartBeatHandler());
        } else {
            /* 其他模式 发送心跳包到客户端确认*/
            p.addLast(new ServerHeartBeatHandler());
        }
        /* 是否打开日志*/
        if (httpPierceConfig.isLogger()) {
            p.addLast(new LoggingHandler());
        }
        /* http代理服务器*/
        if (proxyType == ProxyType.http) {
            p.addLast("httpcode", new HttpServerCodec());
            p.addLast(new ChunkedWriteHandler());
            p.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 100));
            p.addLast("httpProxy", new HttpProxyHandler(proxyConfig.isAuth()));
        }
        /* http转发服务器(内网穿透)*/
        else if (proxyType == ProxyType.http_server) {
            p.addLast("httpcode", new HttpServerCodec());
            p.addLast(new ChunkedWriteHandler());
            p.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 100));
            p.addLast("httpServerHandler", new HttpServerHandler());
        } else if (proxyType == ProxyType.http_server_route) {
            p
                    .addLast(new LengthFieldBasedFrameDecoder(5*1024 * 1024, 0, 4))
                    .addLast(new HttpProtocolCodec())
                    .addLast(new HttpAuthHandler(new DefaultConnectionValidate(), proxyConfig))
                    .addLast(new HttpProtocolHandler());
        } else if (proxyType == ProxyType.socks) {
            p.addFirst(new Socks5CommandRequestDecoder());
            if (proxyConfig.isAuth()) {
                /* 添加验证机制*/
                p.addFirst(new Socks5PasswordAuthRequestHandler());
                p.addFirst(new Socks5PasswordAuthRequestDecoder());
            }
            p.addFirst(new Socks5InitialAuthHandler(proxyConfig.isAuth()));
            p.addFirst(Socks5ServerEncoder.DEFAULT);
            p.addFirst(new Socks5InitialRequestDecoder());
            /* Socks connection handler */
            p.addLast(new Socks5CommandRequestHandler(proxyConfig));
            //p.addFirst(new StatisticsHandler(true, true));

        } else if (proxyType == ProxyType.proxy_server) {
            p
                    .addLast(new LengthFieldBasedFrameDecoder(5*1024 * 1024, 0, 4))
                    .addLast(new MyByteToMessageCodec())
                    .addLast(new AuthHandler(new DefaultConnectionValidate(), proxyConfig))
                    .addLast(new MyProtocolHandler());
        }


    }
}
