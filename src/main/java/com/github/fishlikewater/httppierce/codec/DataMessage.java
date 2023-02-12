package com.github.fishlikewater.httppierce.codec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
public class DataMessage implements Message{

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
     * 注册服务名[多服务以,分割]
     */
    private String registerName;

    /**
     *
     * url
     */
    private String url;

    /**
     *
     * 请求头
     */
    private Map<String, String> heads;

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
    private byte[] bytes;


}