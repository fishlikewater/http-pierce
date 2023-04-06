package com.github.fishlikewater.httppierce;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.kit.KryoUtil;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月19日 16:47
 **/
public class KryoTest {


    @Test
    public void testKryoSerializable() {
        final SysMessage sysMessage = new SysMessage()
                .setId(IdUtil.getSnowflakeNextId())
                .setCommand(Command.AUTH)
                .setState(1)
                .setToken("1111");
        final byte[] bytes = KryoUtil.writeObjectToByteArray(sysMessage);
        System.out.println(bytes.length);
        final SysMessage sysMessage1 = KryoUtil.readObjectFromByteArray(bytes, SysMessage.class);
    }

    @Test
    public void testRand() {
        Set<Integer> set = new TreeSet<>();
        SecureRandom random =  RandomUtil.getSecureRandom();
        for (int i = 0; i < 100; i++) {
            int a = random.nextInt(1,34);
            set.add(a);
            if (set.size() == 6){
                break;
            }
        }
        set.forEach(a->{
            System.out.print(a + "--");
        });
        System.out.print(random.nextInt(1,17));
    }

}
