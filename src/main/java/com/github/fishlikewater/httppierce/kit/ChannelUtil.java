package com.github.fishlikewater.httppierce.kit;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.server.DynamicHttpBoot;
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

    public final static TimedCache<Long, Channel> TIMED_CACHE = CacheUtil.newTimedCache(120000);

    public final static AttributeKey<Map<String, HttpPierceClientConfig.HttpMapping>> CLIENT_FORWARD = AttributeKey.newInstance("CLIENT_FORWARD");

    public final static Map<String, Channel> ROUTE_MAPPING = new ConcurrentHashMap<>();

    public final static Map<Long, Channel> REQUEST_MAPPING = new ConcurrentHashMap<>();

    public final static AttributeKey<Long> HTTP_CHANNEL = AttributeKey.newInstance("HTTP_CHANNEL");

    public final static AttributeKey<Boolean> HTTP_UPGRADE = AttributeKey.newInstance("HTTP_UPGRADE");

    public final static AttributeKey<List<DynamicHttpBoot>> CHANNEL_DYNAMIC_HTTP_BOOT = AttributeKey.newInstance("DYNAMIC_HTTP_BOOT");

    public final static AttributeKey<List<String>> REGISTER_CHANNEL = AttributeKey.newInstance("REGISTER_CHANNEL");

    public final static Map<String, DynamicHttpBoot> DYNAMIC_HTTP_BOOT = new ConcurrentHashMap<>();



}
