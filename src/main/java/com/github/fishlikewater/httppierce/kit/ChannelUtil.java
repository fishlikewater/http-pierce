package com.github.fishlikewater.httppierce.kit;

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

    private ChannelUtil() {}

    public static final Map<String, ConnectionStateInfo> stateMap = new ConcurrentHashMap<>(10);

    public static final AttributeKey<Map<String, ServiceMapping>> CLIENT_FORWARD = AttributeKey.newInstance("CLIENT_FORWARD");

    public static final Map<String, Channel> ROUTE_MAPPING = new ConcurrentHashMap<>();

    public static final Map<Long, Channel> REQUEST_MAPPING = new ConcurrentHashMap<>();

    public static final AttributeKey<List<Long>> HTTP_CHANNEL = AttributeKey.newInstance("HTTP_CHANNEL");

    public static final AttributeKey<Long> TCP_FLAG = AttributeKey.newInstance("TCP_FLAG");

    public static final AttributeKey<Boolean> HTTP_UPGRADE = AttributeKey.newInstance("HTTP_UPGRADE");

    public static final AttributeKey<List<DynamicTcpBoot>> CHANNEL_DYNAMIC_BOOT = AttributeKey.newInstance("DYNAMIC_HTTP_BOOT");

    public static final AttributeKey<List<String>> REGISTER_CHANNEL = AttributeKey.newInstance("REGISTER_CHANNEL");

    public static final Map<String, DynamicTcpBoot> DYNAMIC_BOOT = new ConcurrentHashMap<>();


}
