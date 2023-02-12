package com.github.fishlikewater.httppierce.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  配置文件
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:44
 **/
@Data
@Component
@EqualsAndHashCode
@ConfigurationProperties("http.pierce")
public class HttpPierceConfig {

    /**
     * 启动的服务类型，包括客户端与服务端
     * */
    private BootType bootType;

    /**
     * 服务端监听的地址
     * */
    private String address = "0.0.0.0";

    /**
     * 服务端与客户端通信的端口
     * */
    private int transferPort;

    /**
     * 开放给公网的http端口
     * */
    private int httpServerPort;


    /**
     * 心跳检测间隔，默认30s
     * */
    private long timeout = 30;


    /**
     * 是否开启日志
     * */
    private boolean logger;



    public enum BootType{
        client,server;
    }


}


