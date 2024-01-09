package com.github.fishlikewater.httppierce.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 用于检测channel的心跳handler 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 * </p>
 *
 * @author fishl
 */
@Slf4j
public class ServerHeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                log.info("The server actively closes the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

}
