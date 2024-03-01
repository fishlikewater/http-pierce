package com.github.fishlikewater.httppierce.api;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author fishlikewater@126.com
 */

@Getter
@SuppressWarnings("unused")
public enum CodeEnum {
    /**
     * 200-表示业务执行通畅，执行成功
     */
    SUCCESS("200", "执行成功"),

    PARAMETER_ERROR("400", "参数异常"),

    UNAUTHORIZED("401", "需身份验证"),

    FORBIDDEN("403", "无权限"),

    NOT_FOUND("404", "找不到资源"),

    BUSINESS_FAILED("417", "业务执行失败"),

    METHOD_NOT_ALLOWED("405", "禁止该访问方法"),

    /**
     * 系统异常
     */
    SYSTEM_ERROR("500", "服务器内部错误"),

    TOKEN_OVERDUE("000", "token过期"),

    REFRESH_TOKEN_OVERDUE("001", "refreshToken过期"),

    BAD_GATEWAY("502", "网关或者代理工作的服务器异常"),

    SERVICE_UNAVAILABLE("503", "服务器无法服务");

    /**
     * 枚举值
     * -- GETTER --
     *
     */
    private final String code;

    /**
     * 枚举描述
     * -- GETTER --
     *
     */
    private final String message;

    /**
     * 构造一个<code>DomainResultCodeEnum</code>枚举对象
     *
     * @param code    状态码
     * @param message 消息
     */
    CodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @return Returns the code.
     */
    public String code() {
        return code;
    }

    /**
     * @return Returns the message.
     */
    public String message() {
        return message;
    }

    /**
     * 通过枚举<code>code</code>获得枚举
     *
     * @param code 状态码
     * @return CodeEnum
     */
    public static CodeEnum getByCode(String code) {
        for (CodeEnum codeEnum : values()) {
            if (codeEnum.getCode().equals(code)) {
                return codeEnum;
            }
        }
        return null;
    }

    /**
     * 获取全部枚举
     *
     * @return List<DomainResultCodeEnum>
     */
    public List<CodeEnum> getAllEnum() {
        List<CodeEnum> list = new ArrayList<>();
        Collections.addAll(list, values());
        return list;
    }

    /**
     * 获取全部枚举值
     *
     * @return List<String>
     */
    public List<String> getAllEnumCode() {
        List<String> list = new ArrayList<>();
        for (CodeEnum codeEnum : values()) {
            list.add(codeEnum.code());
        }
        return list;
    }
}

