package com.github.fishlikewater.httppierce;

import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
import com.github.fishlikewater.httppierce.server.HttpBoot;
import com.github.fishlikewater.httppierce.server.ServerBoot;
import com.github.fishlikewater.httppierce.server.ShutDownSignalHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fishl
 */
@SpringBootApplication
@RequiredArgsConstructor
public class HttpPierceApplication implements CommandLineRunner{

    private final HttpPierceConfig httpPierceConfig;

    public static void main(String[] args) {
        SpringApplication.run(HttpPierceApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        if (httpPierceConfig.getBootType() == HttpPierceConfig.BootType.server){
            final ServerBoot serverBoot = new ServerBoot(httpPierceConfig);
            serverBoot.start();
            final HttpBoot httpBoot = new HttpBoot(httpPierceConfig);
            httpBoot.start();
            final ShutDownSignalHandler shutDownSignalHandler = new ShutDownSignalHandler();
            shutDownSignalHandler.registerSignal("TERM", serverBoot, httpBoot);
            shutDownSignalHandler.registerSignal("INT", serverBoot, httpBoot);
        }
    }
}
