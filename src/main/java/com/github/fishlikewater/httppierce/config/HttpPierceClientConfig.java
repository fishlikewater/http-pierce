package com.github.fishlikewater.httppierce.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * <p>
 * 配置文件
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 22:44
 **/
@Data
@Component
@EqualsAndHashCode
@ConfigurationProperties("http.pierce.client")
public class HttpPierceClientConfig {


    /**
     * 服务端监听的地址
     **/
    private String serverAddress;

    /**
     * 服务端与客户端通信的端口
     **/
    private int serverPort;


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
     * 连接失败，重连间隔时间(单位秒)
     **/
    private Duration retryTime = Duration.ofSeconds(30);

    /**
     * 每一帧最大字节
     */
    private DataSize maxFrameLength = DataSize.ofBytes(5 * 1024 * 1024);


    /**
     * 客户端映射配置
     **/
    private HttpMapping[] httpMappings;


    @Data
    public static class HttpMapping {

        /**
         * 客户端映射地址
         **/
        private String address;

        /**
         * 客户端映射端口
         **/
        private int port;

        /**
         * 客户端注册名称
         **/
        private String registerName;

        /**
         * 服务端是否新开端口，即服务端新开一个端口映射该服务
         **/
        private boolean newServerPort;

        /**
         * 服务端新开端口(注意不要和服务器上已有服务端口冲突)
         **/
        private int newPort;

        /**
         * 客户端 发送请求到具体服务时是否要截除注册名
         **/
        private boolean delRegisterName;

        /**
         * 外网开放协议 http https
         **/
        private ProtocolEnum protocol;


    }


}



