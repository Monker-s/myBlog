package com.monker.myblog.common;

import java.time.Instant;
import java.util.UUID;

/**
 * 文件用途：定义全站统一响应体。
 * 作用说明：所有 Controller 默认通过该对象向前端返回业务码、消息体、数据和请求追踪信息。
 *
 * @param <T> 响应数据类型
 */
public record Result<T>(
        int code,
        String message,
        T data,
        String requestId,
        Instant timestamp
) {

    /**
     * 函数用途：构建一个默认成功响应。
     *
     * @param data 业务数据
     * @return 包装后的成功响应
     * @param <T> 响应数据类型
     */
    public static <T> Result<T> success(T data) {
        return of(ResultCode.SUCCESS, data);
    }

    /**
     * 函数用途：构建一个带自定义提示语的成功响应。
     *
     * @param message 成功提示语
     * @param data 业务数据
     * @return 包装后的成功响应
     * @param <T> 响应数据类型
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(
                ResultCode.SUCCESS.getCode(),
                message,
                data,
                UUID.randomUUID().toString(),
                Instant.now()
        );
    }

    /**
     * 函数用途：构建一个失败响应。
     *
     * @param resultCode 统一错误码枚举
     * @param message 错误提示语
     * @return 包装后的失败响应
     * @param <T> 响应数据类型
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(
                resultCode.getCode(),
                message,
                null,
                UUID.randomUUID().toString(),
                Instant.now()
        );
    }

    /**
     * 函数用途：复用统一错误码和默认消息生成响应。
     *
     * @param resultCode 统一业务码
     * @param data 业务数据
     * @return 包装后的响应对象
     * @param <T> 响应数据类型
     */
    private static <T> Result<T> of(ResultCode resultCode, T data) {
        return new Result<>(
                resultCode.getCode(),
                resultCode.getMessage(),
                data,
                UUID.randomUUID().toString(),
                Instant.now()
        );
    }
}
