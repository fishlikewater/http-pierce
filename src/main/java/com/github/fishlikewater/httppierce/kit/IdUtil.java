package com.github.fishlikewater.httppierce.kit;

/**
 * {@code IdUtil}
 * Id生成器
 *
 * @author zhangxiang
 * @date 2024/02/04
 * @since 1.0.1
 */
public class IdUtil {

    public static long generateId() {
        return cn.hutool.core.util.IdUtil.getSnowflakeNextId();
    }

}
