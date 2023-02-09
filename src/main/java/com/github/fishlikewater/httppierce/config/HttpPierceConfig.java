package com.github.fishlikewater.httppierce.config;

import com.github.fishlikewater.httppierce.client.ClientBoot;
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

    private BootType bootType;



    private enum BootType{
        client,server;
    }


}



