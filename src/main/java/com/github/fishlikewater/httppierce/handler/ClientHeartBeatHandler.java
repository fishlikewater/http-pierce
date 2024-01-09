package com.github.fishlikewater.httppierce.handler;


import cn.hutool.core.util.IdUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于检测channel的心跳handler
 * 继承ChannelInboundHandlerAdapter，从而不需要实现channelRead0方法
 */
@Slf4j
public class ClientHeartBeatHandler extends ChannelInboundHandlerAdapter {

    public static final SysMessage HEARTBEAT_SEQUENCE = new SysMessage()
            .setId(IdUtil.getSnowflakeNextId())
            .setCommand(Command.HEALTH);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            //IdleStateEvent event = (IdleStateEvent) evt;        // 强制类型转换
            ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE)
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            log.warn("failed to send heartbeat packet...");
                        }

                    });
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }

}
