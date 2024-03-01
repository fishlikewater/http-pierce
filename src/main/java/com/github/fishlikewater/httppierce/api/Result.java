package com.github.fishlikewater.httppierce.api;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.UUID;

/**
 * <p>
 * 返回基本结构
 * </p>
 *
 * @author fishlikewater@126.com
 */
@Data
@Accessors(chain = true)
@SuppressWarnings("unused")
public class Result<T> implements Serializable {

    private static final CodeEnum DEFAULT_CODE = CodeEnum.SUCCESS;

    protected String code;

    protected String message;

    private T data;

    private String requestId;

    public Result() {
        setCode(DEFAULT_CODE);
        setMessage(DEFAULT_CODE.message());
        requestId = uuid();
    }

    public Result(T data) {
        this.data = data;
        setCode(DEFAULT_CODE);
        setMessage(DEFAULT_CODE.message());
        requestId = uuid();
    }

    public Result(CodeEnum code) {
        setCode(code);
        setMessage(code.message());
        requestId = uuid();
    }

    public Result(CodeEnum code, String message) {
        this.code = code.code();
        this.message = message;
        requestId = uuid();
    }

    public static <T> Result<T> of(T data, String message, CodeEnum code) {
        final Result<T> response = new Result<>(code, message);
        response.setData(data);
        return response;
    }

    public static <T> Result<T> of(T data, CodeEnum code) {
        final Result<T> response = new Result<>(code);
        response.setData(data);
        return response;
    }

    public static <T> Result<T> of(String message) {
        return new Result<>(DEFAULT_CODE, message);
    }

    public static <T> Result<T> of(T data) {
        return new Result<>(data);
    }

    /**
     * 设置响应消息
     *
     * @param code    请求状态码
     * @param message 消息
     * @param data    请求结果
     */
    public void setContent(CodeEnum code, String message, T data) {
        this.code = code.code();
        this.message = message;
        this.data = data;
        this.requestId = uuid();
    }

    /**
     * 生成UUID
     *
     * @return UUID
     */
    public String uuid() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    public Result<T> setCode(CodeEnum code) {
        this.code = code.code();
        return this;
    }
}
