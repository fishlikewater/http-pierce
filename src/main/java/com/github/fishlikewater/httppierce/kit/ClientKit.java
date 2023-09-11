package com.github.fishlikewater.httppierce.kit;

import cn.hutool.core.util.IdUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.config.ProtocolEnum;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年09月09日 16:20
 **/
public class ClientKit {

    private static Channel channel;


    public static void setChannel(Channel channel){

        ClientKit.channel = channel;
    }

    public static void registerService(ServiceMapping serviceMapping){
        final SysMessage registerMsg = new SysMessage();
        registerMsg.setCommand(Command.REGISTER)
                .setId(IdUtil.getSnowflakeNextId())
                .setRegister(new SysMessage.Register()
                        .setRegisterName(serviceMapping.getRegisterName())
                        .setProtocol(ProtocolEnum.valueOf(serviceMapping.getProtocol()))
                        .setNewServerPort(serviceMapping.getNewServerPort() == 1)
                        .setNewPort(serviceMapping.getNewPort()));
        channel.writeAndFlush(registerMsg);
    }

    public static void  reRegister(String registerName){
        final Map<String, ServiceMapping> mappingMap = channel.attr(ChannelUtil.CLIENT_FORWARD).get();
        for (Map.Entry<String, ServiceMapping> mappingEntry : mappingMap.entrySet()) {
            final String key = mappingEntry.getKey();
            if (key.equals(registerName)) {
                final ServiceMapping value = mappingEntry.getValue();
                registerService(value);
                break;
            }
        }
    }

    public static void addMapping(ServiceMapping serviceMapping){
        final Map<String, ServiceMapping> stringServiceMappingMap = channel.attr(ChannelUtil.CLIENT_FORWARD).get();
        final ServiceMapping serviceMapping1 = stringServiceMappingMap.get(serviceMapping.getRegisterName());
        if(Objects.isNull(serviceMapping1)){
            stringServiceMappingMap.put(serviceMapping.getRegisterName(), serviceMapping);
            registerService(serviceMapping);
        }
    }

    public static void cancelRegister(String registerName){
        final Map<String, ServiceMapping> mappingMap = channel.attr(ChannelUtil.CLIENT_FORWARD).get();
        for (Map.Entry<String, ServiceMapping> mappingEntry : mappingMap.entrySet()) {
            final String key = mappingEntry.getKey();
            if (key.equals(registerName)) {
                final SysMessage cancel = new SysMessage();
                cancel.setCommand(Command.CANCEL_REGISTER);
                cancel.setId(IdUtil.getSnowflakeNextId());
                cancel.setRegister(new SysMessage.Register()
                        .setRegisterName(registerName));
                channel.writeAndFlush(cancel);
                break;
            }
        }
    }



}

