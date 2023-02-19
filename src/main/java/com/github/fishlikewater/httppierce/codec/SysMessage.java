package com.github.fishlikewater.httppierce.codec;

import lombok.Builder;
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
    private Register register;


    /**
     *
     * 消息类型
     */
    private Command command;

    @Data
    @Builder
    @EqualsAndHashCode
    public static class Register{
        /**
         * 客户端注册名称
         **/
        private String registerName;

        /**
         * 服务端是否新开端口，即服务端新开一个端口映射该服务
         **/
        private boolean newServerPort;

        /**
         * 服务端新开端口(注意不要和服务器上已有服务端口冲突)
         **/
        private int newPort;

    }

}
