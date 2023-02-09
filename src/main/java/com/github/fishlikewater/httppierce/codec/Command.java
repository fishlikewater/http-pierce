package com.github.fishlikewater.httppierce.codec;

import lombok.Getter;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 10:22
 **/
public enum Command implements Serializable {

    //验证
    AUTH(0),
    //客户端注册
    REGISTER(1),
    //请求
    REQUEST(2),
    //响应
    RESPONSE(3),
    //心跳
    HEALTH(4),
    //关闭
    CLOSE(5);
    @Getter
    private final int code;

    Command(int code){
        this.code = code;
    }

    public static Command getInstance(int code){
        for (Command value : Command.values()) {
            if (value.code == code){
                return value;
            }
        }
        return null;
    }
}
