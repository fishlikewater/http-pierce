package com.github.fishlikewater.httppierce;

import com.esotericsoftware.kryo.Kryo;
import com.github.fishlikewater.httppierce.codec.DataMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HttpPierceApplicationTests {

    @Test
    void contextLoads() {
        final Kryo kryo = new Kryo();
        kryo.register(DataMessage.class);

    }

}
