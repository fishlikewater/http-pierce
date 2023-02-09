package com.github.fishlikewater.httppierce.codec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *  系统通用消息
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 10:17
 **/
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class SysMessage implements Message{

    /**
     *
     * 消息id
     */
    private Long id;

    /**
     *
     * 验证类容
     */
    private String token;

    /**
     *
     * 状态
     */
    private int state;


    /**
     *
     * 客户端注册信息
     */
    private String registerNames;


    /**
     *
     * 消息类型
     */
    private Command command;

}
