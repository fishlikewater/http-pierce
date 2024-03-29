package com.github.fishlikewater.httppierce.codec;

/**
 * <p>
 * 消息统一接口
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 10:32
 **/
public interface Message {

    /**
     * 获取消息id
     *
     * @return {@code Long}
     */
    default Long getId() {
        return 0L;
    }
}
