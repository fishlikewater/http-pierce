package com.github.fishlikewater.httppierce.handler;

import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年08月17日 14:59
 **/
public class HandlerKit {


    private HandlerKit() {
        throw new IllegalStateException("Utility class");
    }

    public static void upWebSocket(Channel channel, Channel clientChannel, Long id) {
        //协议升级处理
        channel.pipeline().remove(HttpRequestDecoder.class);
        channel.pipeline().remove(HttpObjectAggregator.class);
        if (channel.pipeline().get(ChunkedWriteHandler.class) != null) {
            channel.pipeline().remove(ChunkedWriteHandler.class);
        }
        if (channel.pipeline().get(HttpServerHandler.class) != null) {
            channel.pipeline().remove(HttpServerHandler.class);
        }
        if (channel.pipeline().get(DynamicHttpServerHandler.class) != null) {
            channel.pipeline().remove(DynamicHttpServerHandler.class);
        }
        channel.pipeline().addFirst(new ByteArrayDecoder());
        channel.pipeline().addLast(new WebSocketHandler(id, clientChannel));
        channel.attr(ChannelUtil.HTTP_UPGRADE).set(null);
    }

}
