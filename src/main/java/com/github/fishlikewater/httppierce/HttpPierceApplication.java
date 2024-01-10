package com.github.fishlikewater.httppierce;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.setting.Setting;
import com.github.fishlikewater.httppierce.client.ClientBoot;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.kit.ClientKit;
import com.github.fishlikewater.httppierce.kit.LoggerUtil;
import com.github.fishlikewater.httppierce.kit.SslUtil;
import com.github.fishlikewater.httppierce.server.HttpBoot;
import com.github.fishlikewater.httppierce.server.ServerBoot;
import com.github.fishlikewater.httppierce.server.ShutDownSignalHandler;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.net.ssl.SSLException;
import java.io.File;

/**
 * @author fishl
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@EnableTransactionManagement
@MapperScan("com.github.fishlikewater.httppierce.mapper")
public class HttpPierceApplication implements CommandLineRunner {

    private final HttpPierceServerConfig httpPierceServerConfig;
    private final HttpPierceClientConfig httpPierceClientConfig;
    private final HttpPierceConfig httpPierceConfig;

    public static void main(String[] args) {
        final String web = new Setting("web.setting").getStr("server.type", "web");
        SpringApplication app = new SpringApplication(HttpPierceApplication.class);
        if ("none".equals(web)) {
            app.setWebApplicationType(WebApplicationType.NONE);
        }
        app.run(args);
    }

    @Override
    public void run(String... args) throws SSLException {
        if (httpPierceConfig.isLogger()) {
            LoggerUtil.setLogPath(httpPierceConfig.getLogPath());
        }
        final ShutDownSignalHandler shutDownSignalHandler = new ShutDownSignalHandler();

        switch (httpPierceConfig.getBootType()) {
            case SERVER:
                if (httpPierceServerConfig.getSslConfig().isEnable()) {
                    SslUtil.init(httpPierceServerConfig.getSslConfig());
                }
                final ServerBoot serverBoot = new ServerBoot(httpPierceServerConfig, httpPierceConfig);
                serverBoot.start();
                final HttpBoot httpBoot = new HttpBoot(httpPierceServerConfig, httpPierceConfig);
                httpBoot.start();
                shutDownSignalHandler.registerSignal("TERM", serverBoot, httpBoot);
                shutDownSignalHandler.registerSignal("INT", serverBoot, httpBoot);
                break;
            case CLIENT:
                initTable();
                final ClientBoot clientBoot = new ClientBoot(httpPierceClientConfig);
                ClientKit.setClientBoot(clientBoot);
                clientBoot.start();
                shutDownSignalHandler.registerSignal("TERM", clientBoot);
                shutDownSignalHandler.registerSignal("INT", clientBoot);
                break;
            default:
                log.error("未知启动类型");
                break;
        }
    }

    private void initTable() {
        String tableName = "'service_mapping'";
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name= " + tableName;
        final Row row = Db.selectOneBySql(sql);
        if (row == null || row.isEmpty()) {
            final File file = FileUtil.file("db/init.sql");
            FileReader fileReader = new FileReader(file);
            final String initSql = fileReader.readString();
            Db.updateBySql(initSql);
        }
    }
}
