package com.github.fishlikewater.httppierce.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

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
@ConfigurationProperties("http.pierce.server")
public class HttpPierceServerConfig {

    /**
     * 服务端监听的地址
     **/
    private String address = "0.0.0.0";

    /**
     * 服务端与客户端通信的端口
     **/
    private int transferPort;

    /**
     * 开放给公网的http端口
     **/
    private int httpServerPort;


    /**
     * 心跳检测间隔，默认30s
     **/
    private Duration timeout = Duration.ofSeconds(30);


    /**
     * 是否开启日志
     **/
    private boolean logger;

    /**
     * 验证token
     **/
    private String token;

    /**
     *
     * http连接保持时间
     */
    private Duration keepTimeOut = Duration.ofSeconds(120);

    /**
     *
     * 每一帧最大字节
     */
    private DataSize maxFrameLength = DataSize.ofBytes(5*1024 * 1024);


    /**
     *
     * HttpObjectAggregator 大小
     */
    private DataSize httpObjectSize = DataSize.ofBytes(5*1024 * 1024);


}



