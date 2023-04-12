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
     **/
    private BootType bootType;

    /**
     * 是否开启访问记录日志
     **/
    private boolean logger;

    /**
     * 访问记录日志路劲
     **/
    private String logPath;


}



