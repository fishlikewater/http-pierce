package com.github.fishlikewater.httppierce.kit;

import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.entity.ConnectionStateInfo;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.github.fishlikewater.httppierce.server.DynamicTcpBoot;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 连接属性
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月17日 15:55
 **/
public class ChannelUtil {

    public final static Map<String, ConnectionStateInfo> stateMap = new ConcurrentHashMap<>(10);

    public final static AttributeKey<Map<String, ServiceMapping>> CLIENT_FORWARD = AttributeKey.newInstance("CLIENT_FORWARD");

    public final static Map<String, Channel> ROUTE_MAPPING = new ConcurrentHashMap<>();

    public final static Map<Long, Channel> REQUEST_MAPPING = new ConcurrentHashMap<>();

    public final static AttributeKey<List<Long>> HTTP_CHANNEL = AttributeKey.newInstance("HTTP_CHANNEL");

    public final static AttributeKey<Long> TCP_FLAG = AttributeKey.newInstance("TCP_FLAG");

    public final static AttributeKey<Boolean> HTTP_UPGRADE = AttributeKey.newInstance("HTTP_UPGRADE");

    public final static AttributeKey<List<DynamicTcpBoot>> CHANNEL_DYNAMIC_BOOT = AttributeKey.newInstance("DYNAMIC_HTTP_BOOT");

    public final static AttributeKey<List<String>> REGISTER_CHANNEL = AttributeKey.newInstance("REGISTER_CHANNEL");

    public final static Map<String, DynamicTcpBoot> DYNAMIC_BOOT = new ConcurrentHashMap<>();


}
