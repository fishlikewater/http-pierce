package com.github.fishlikewater.httppierce;

import cn.hutool.setting.Setting;
import com.github.fishlikewater.httppierce.config.Constant;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author fishl
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.github.fishlikewater.httppierce.mapper")
public class HttpPierceApplication {

    public static void main(String[] args) {
        final String web = new Setting("web.setting").getStr("server.type", "web");
        SpringApplication app = new SpringApplication(HttpPierceApplication.class);
        if (Constant.NONE.equals(web)) {
            app.setWebApplicationType(WebApplicationType.NONE);
        }
        app.run(args);
    }
}
