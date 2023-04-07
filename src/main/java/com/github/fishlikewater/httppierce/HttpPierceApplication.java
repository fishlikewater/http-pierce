package com.github.fishlikewater.httppierce;

import com.github.fishlikewater.httppierce.client.ClientBoot;
import com.github.fishlikewater.httppierce.config.BootType;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.config.HttpPierceServerConfig;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.server.HttpBoot;
import com.github.fishlikewater.httppierce.server.ServerBoot;
import com.github.fishlikewater.httppierce.server.ShutDownSignalHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fishl
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class HttpPierceApplication implements CommandLineRunner{

    private final HttpPierceServerConfig httpPierceServerConfig;
    private final HttpPierceClientConfig httpPierceClientConfig;
    private final HttpPierceConfig httpPierceConfig;

    public static void main(String[] args) {
        SpringApplication.run(HttpPierceApplication.class, args);

    }

    @Override
    public void run(String... args) {
        if (httpPierceConfig.getBootType() == BootType.server){
            final ServerBoot serverBoot = new ServerBoot(httpPierceServerConfig);
            serverBoot.start();
            final HttpBoot httpBoot = new HttpBoot(httpPierceServerConfig);
            httpBoot.start();
            final ShutDownSignalHandler shutDownSignalHandler = new ShutDownSignalHandler();
            shutDownSignalHandler.registerSignal("TERM", serverBoot, httpBoot);
            shutDownSignalHandler.registerSignal("INT", serverBoot, httpBoot);
            ChannelUtil.TIMED_CACHE.schedulePrune(5000L);
        }
        if (httpPierceConfig.getBootType() == BootType.client){
            final ClientBoot clientBoot = new ClientBoot(httpPierceClientConfig);
            clientBoot.start();
            final ShutDownSignalHandler shutDownSignalHandler = new ShutDownSignalHandler();
            shutDownSignalHandler.registerSignal("TERM", clientBoot);
            shutDownSignalHandler.registerSignal("INT", clientBoot);
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "本次运行版本: \t" + httpPierceConfig.getVersion() + "\n\t" +
                "本次版本构建时间: \t" + httpPierceConfig.getBuildTime() + "\n\t" +
                "----------------------------------------------------------");
    }
}
