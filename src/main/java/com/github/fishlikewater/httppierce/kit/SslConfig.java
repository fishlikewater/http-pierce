package com.github.fishlikewater.httppierce.kit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  ssl 配置
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月02日 11:41
 **/
@Data
@Component
@ConfigurationProperties("proxy.ssl")
public class SslConfig {
    /**是否开启ssl*/
    private boolean enable = false;
    /**是否开启双向验证*/
    private boolean needClientAuth = false;
    /**密匙库地址*/
    private String pkPath;
    /** 签名证书地址*/
    private String caPath;
    /**证书密码*/
    private String passwd;
}
