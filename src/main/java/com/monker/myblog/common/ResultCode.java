package com.monker.myblog.common;

/**
 * 文件用途：定义项目统一业务状态码。
 * 作用说明：Controller、Service 和全局异常处理统一通过该枚举表达成功与失败语义。
 */
public enum ResultCode {

    SUCCESS(0, "操作成功"),
    INVALID_ARGUMENT(4220, "参数校验失败"),
    UNAUTHORIZED(4010, "未认证或认证已过期"),
    FORBIDDEN(4030, "无权访问当前资源"),
    NOT_FOUND(4040, "请求资源不存在"),
    USER_NOT_FOUND(4041, "用户不存在"),
    CONFLICT(4090, "资源状态冲突"),
    TOO_MANY_REQUESTS(4290, "请求过于频繁"),
    UPDATE_FAILED(5001, "更新失败"),
    NOT_IMPLEMENTED(5010, "功能骨架已建立，待接入真实业务实现"),
    INTERNAL_ERROR(5000, "服务内部错误");

    private final int code;
    private final String message;

    /**
     * 函数用途：根据状态码和默认消息创建业务码枚举。
     *
     * @param code 业务码
     * @param message 默认消息
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 函数用途：返回当前枚举对应的业务码。
     *
     * @return 业务码
     */
    public int getCode() {
        return code;
    }

    /**
     * 函数用途：返回当前枚举对应的默认提示语。
     *
     * @return 默认提示语
     */
    public String getMessage() {
        return message;
    }
}
