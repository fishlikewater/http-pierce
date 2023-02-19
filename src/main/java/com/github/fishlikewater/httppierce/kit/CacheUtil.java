package com.github.fishlikewater.httppierce.kit;

import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月17日 15:55
 **/
public class CacheUtil {

    public final static AttributeKey<Map<String, HttpPierceClientConfig.HttpMapping>> CLIENT_FORWARD = AttributeKey.newInstance("CLIENT_FORWARD");


    public final static AttributeKey<Map<String, Channel>> SERVER_FORWARD = AttributeKey.newInstance("SERVER_FORWARD");

}
