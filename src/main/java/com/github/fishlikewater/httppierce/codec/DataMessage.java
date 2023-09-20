package com.github.fishlikewater.httppierce.codec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  http消息
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月09日 10:26
 **/
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@NoArgsConstructor
public class DataMessage implements Message, Serializable {

    /**
     *
     * 每次传输消息的id
     **/
    private long id;

    /**
     *
     * 消息类型
     **/
    private Command command;


    /**
     *
     * 目标服务名
     */
    private String dstServer;

    /**
     *
     * url
     */
    private String url;

    /**
     *
     * 请求头
     */
    private Map<String, String> heads = new HashMap<>();

    /**
     *
     * 请求方法
     */
    private String method;


    /**
     *
     * http版本
     */
    private String version;


    /**
     *
     * 响应码
     */
    private int code;


    /**
     *
     * 消息类容
     **/
    private byte[] bytes = new byte[0];


}
