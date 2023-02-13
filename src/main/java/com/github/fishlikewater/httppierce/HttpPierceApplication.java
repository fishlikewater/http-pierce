package com.github.fishlikewater.httppierce;

import com.github.fishlikewater.httppierce.config.HttpPierceConfig;
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

    }
}
