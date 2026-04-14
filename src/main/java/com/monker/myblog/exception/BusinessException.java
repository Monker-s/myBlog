package com.monker.myblog.exception;

import com.monker.myblog.common.ResultCode;

/**
 * 文件用途：定义项目级业务异常。
 * 作用说明：当服务层出现可预期的业务错误时，统一抛出该异常并由全局异常处理器转换成标准响应。
 */
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    /**
     * 函数用途：使用统一业务码创建业务异常。
     *
     * @param resultCode 统一业务码
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * 函数用途：使用统一业务码和自定义消息创建业务异常。
     *
     * @param resultCode 统一业务码
     * @param message 自定义异常消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    /**
     * 函数用途：返回当前业务异常对应的统一业务码。
     *
     * @return 业务码枚举
     */
    public ResultCode getResultCode() {
        return resultCode;
    }
}
