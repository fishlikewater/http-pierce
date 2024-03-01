package com.github.fishlikewater.httppierce.config;

import lombok.Getter;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月16日 19:03
 **/
@Getter
public enum BootType {

    /**
     * 客户端
     */
    CLIENT("client"),
    /**
     * 服务端
     */
    SERVER("server");

    final String value;

    BootType(String value) {
        this.value = value;
    }
}
