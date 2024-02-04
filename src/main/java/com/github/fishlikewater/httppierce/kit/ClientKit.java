package com.github.fishlikewater.httppierce.kit;

import com.github.fishlikewater.httppierce.client.ClientBoot;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.Message;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.config.ProtocolEnum;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年09月09日 16:20
 **/
public class ClientKit {

    private ClientKit() {}

    private static Channel channel;

    @Getter
    private static final Map<Long, ChannelPromise> PROMISE_MAP = new ConcurrentHashMap<>();

    @Setter
    @Getter
    private static ClientBoot clientBoot;


    public static void setChannel(Channel channel) {

        ClientKit.channel = channel;
    }

    public static void registerService(ServiceMapping serviceMapping, boolean sync) {
        final SysMessage registerMsg = new SysMessage();
        registerMsg.setCommand(Command.REGISTER)
                .setId(IdUtil.generateId())
                .setRegister(new SysMessage.Register()
                        .setRegisterName(serviceMapping.getRegisterName())
                        .setProtocol(ProtocolEnum.valueOf(serviceMapping.getProtocol()))
                        .setNewServerPort(serviceMapping.getNewServerPort() == 1)
                        .setNewPort(serviceMapping.getNewPort()));
        if (sync) {
            syncWaitWriteAndFlush(registerMsg);
        } else {
            channel.writeAndFlush(registerMsg);
        }
    }

    public static void reRegister(String registerName) {
        final Map<String, ServiceMapping> mappingMap = channel.attr(ChannelUtil.CLIENT_FORWARD).get();
        for (Map.Entry<String, ServiceMapping> mappingEntry : mappingMap.entrySet()) {
            final String key = mappingEntry.getKey();
            if (key.equals(registerName)) {
                final ServiceMapping value = mappingEntry.getValue();
                registerService(value, false);
                break;
            }
        }
    }

    public static void addMapping(ServiceMapping serviceMapping) {
        final Map<String, ServiceMapping> stringServiceMappingMap = channel.attr(ChannelUtil.CLIENT_FORWARD).get();
        final ServiceMapping serviceMapping1 = stringServiceMappingMap.get(serviceMapping.getRegisterName());
        if (Objects.isNull(serviceMapping1)) {
            stringServiceMappingMap.put(serviceMapping.getRegisterName(), serviceMapping);
            registerService(serviceMapping, true);
        }
    }

    public static void cancelRegister(String registerName) {
        final Map<String, ServiceMapping> mappingMap = channel.attr(ChannelUtil.CLIENT_FORWARD).get();
        for (Map.Entry<String, ServiceMapping> mappingEntry : mappingMap.entrySet()) {
            final String key = mappingEntry.getKey();
            if (key.equals(registerName)) {
                final SysMessage cancel = new SysMessage();
                SysMessage.Register register = new SysMessage.Register().setRegisterName(registerName);
                cancel.setCommand(Command.CANCEL_REGISTER);
                cancel.setId(IdUtil.generateId());
                cancel.setRegister(register);
                syncWaitWriteAndFlush(cancel);
                break;
            }
        }
    }

    /**
     * 同步发送消息 并等待响应
     *
     * @param msg 消息
     */
    public static void syncWaitWriteAndFlush(Message msg) {
        ChannelPromise promise = channel.newPromise();
        PROMISE_MAP.put(msg.getId(), promise);
        channel.writeAndFlush(msg);
        try {
            promise.await(10, TimeUnit.SECONDS);
            PROMISE_MAP.remove(msg.getId());
        } catch (InterruptedException ignored) {
        }
    }
}

