package com.github.fishlikewater.httppierce.server;

import com.github.fishlikewater.httppierce.codec.ByteArrayCodec;
import com.github.fishlikewater.httppierce.handler.DynamicTcpServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
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
public class DynamicTcpHandlerInitializer extends ChannelInitializer<Channel> {

    private final Channel channel;

    private final String registerName;

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        p.addLast("byte", new ByteArrayCodec());
        p.addLast("tcp_handler", new DynamicTcpServerHandler(channel, registerName));
    }
}
