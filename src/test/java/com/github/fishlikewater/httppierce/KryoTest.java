package com.github.fishlikewater.httppierce;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.github.fishlikewater.httppierce.codec.Command;
import com.github.fishlikewater.httppierce.codec.SysMessage;
import com.github.fishlikewater.httppierce.kit.KryoUtil;
import org.junit.jupiter.api.Test;

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
    public void testKryoSerializable(){
        final SysMessage sysMessage = new SysMessage()
                .setId(IdUtil.getSnowflakeNextId())
                .setCommand(Command.AUTH)
                .setState(1)
                .setToken("1111");
        final byte[] bytes = KryoUtil.writeObjectToByteArray(sysMessage);
        System.out.println(bytes.length);
        final SysMessage sysMessage1 = KryoUtil.readObjectFromByteArray(bytes, SysMessage.class);
        System.out.println(JSONUtil.toJsonStr(sysMessage1));
    }

}
