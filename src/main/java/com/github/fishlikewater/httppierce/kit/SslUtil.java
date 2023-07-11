package com.github.fishlikewater.httppierce.kit;

import cn.hutool.core.lang.Singleton;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年07月11日 21:02
 **/
public class SslUtil {

    private static SslContext sslContext;

    public static SslContext getSslContext(){
        if (Objects.nonNull(sslContext)){
            return sslContext;
        }else {
            throw new RuntimeException("请先配置ssl参数");
        }
    }

    public static void init(HttpPierceServerConfig.SslConfig sslConfig) throws SSLException {
        sslContext = SslContextBuilder.forServer(
                new File(sslConfig.getCaPath()),
                new File(sslConfig.getPkPath())).build();
    }

}
