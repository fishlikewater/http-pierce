package com.github.fishlikewater.httppierce;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import com.github.fishlikewater.httppierce.client.ClientBoot;
import com.github.fishlikewater.httppierce.config.BootType;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
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
public class HttpPierceApplication implements CommandLineRunner{

    private final HttpPierceServerConfig httpPierceServerConfig;
    private final HttpPierceClientConfig httpPierceClientConfig;
    private final HttpPierceConfig httpPierceConfig;

    public static void main(String[] args) {
        SpringApplication.run(HttpPierceApplication.class, args);

    }

    @Override
    public void run(String... args) throws SSLException {
        initTable();
        if (httpPierceConfig.isLogger()){
            LoggerUtil.setLogPath(httpPierceConfig.getLogPath());
        }
        if (httpPierceConfig.getBootType() == BootType.server){
            if (httpPierceServerConfig.getSslConfig().isEnable()){
                SslUtil.init(httpPierceServerConfig.getSslConfig());
            }
            final ServerBoot serverBoot = new ServerBoot(httpPierceServerConfig, httpPierceConfig);
            serverBoot.start();
            final HttpBoot httpBoot = new HttpBoot(httpPierceServerConfig, httpPierceConfig);
            httpBoot.start();
            final ShutDownSignalHandler shutDownSignalHandler = new ShutDownSignalHandler();
            shutDownSignalHandler.registerSignal("TERM", serverBoot, httpBoot);
            shutDownSignalHandler.registerSignal("INT", serverBoot, httpBoot);

        }
        if (httpPierceConfig.getBootType() == BootType.client){
            final ClientBoot clientBoot = new ClientBoot(httpPierceClientConfig);
            clientBoot.start();
            final ShutDownSignalHandler shutDownSignalHandler = new ShutDownSignalHandler();
            shutDownSignalHandler.registerSignal("TERM", clientBoot);
            shutDownSignalHandler.registerSignal("INT", clientBoot);
        }
    }

    private void initTable(){
        String tableName = "'service_mapping'";
        String sql =  "SELECT name FROM sqlite_master WHERE type='table' AND name= " + tableName;
        final Row row = Db.selectOneBySql(sql);
        if (row == null || row.isEmpty()){
            final File file = FileUtil.file("db/init.sql");
            FileReader fileReader = new FileReader(file);
            final String initSql = fileReader.readString();
            Db.updateBySql(initSql);
        }
    }
}
