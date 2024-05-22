package com.github.fishlikewater.httppierce.init;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import com.github.fishlikewater.httppierce.client.ClientBoot;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.kit.ClientKit;
import com.github.fishlikewater.httppierce.kit.LoggerUtil;
import com.github.fishlikewater.httppierce.kit.SslUtil;
import com.github.fishlikewater.httppierce.server.Boot;
import com.github.fishlikewater.httppierce.server.DynamicTcpBoot;
import com.github.fishlikewater.httppierce.server.HttpBoot;
import com.github.fishlikewater.httppierce.server.ServerBoot;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Map;

/**
 * {@code TableInit}
 *
 * @author zhangxiang
 * @version 1.0.0
 * @since 2024/05/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TableInit implements CommandLineRunner {

    private final HttpPierceServerConfig httpPierceServerConfig;

    private final HttpPierceClientConfig httpPierceClientConfig;

    private final HttpPierceConfig httpPierceConfig;

    @Override
    public void run(String... args) throws SSLException {
        if (httpPierceConfig.isLogger()) {
            LoggerUtil.setLogPath(httpPierceConfig.getLogPath());
        }

        switch (httpPierceConfig.getBootType()) {
            case SERVER:
                if (httpPierceServerConfig.getSslConfig().isEnable()) {
                    SslUtil.init(httpPierceServerConfig.getSslConfig());
                }
                final ServerBoot serverBoot = new ServerBoot(httpPierceServerConfig, httpPierceConfig);
                serverBoot.start();
                final HttpBoot httpBoot = new HttpBoot(httpPierceServerConfig, httpPierceConfig);
                httpBoot.start();
                this.registerCloseHook(serverBoot, httpBoot);
                break;
            case CLIENT:
                initTable();
                final ClientBoot clientBoot = new ClientBoot(httpPierceClientConfig);
                ClientKit.setClientBoot(clientBoot);
                clientBoot.start();
                this.registerCloseHook(clientBoot);
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

    private void registerCloseHook(Boot... boot) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received SIGTERM, shutting down...");
            final Map<String, DynamicTcpBoot> dynamicHttpBoot = ChannelUtil.DYNAMIC_BOOT;
            if (!dynamicHttpBoot.isEmpty()) {
                dynamicHttpBoot.forEach((k, v) -> v.stop());
            }
            for (Boot boot1 : boot) {
                boot1.stop();
            }
        }));
    }
}
