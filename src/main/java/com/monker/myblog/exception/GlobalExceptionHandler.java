package com.monker.myblog.exception;

import com.monker.myblog.common.Result;
import com.monker.myblog.common.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 文件用途：定义全局异常处理逻辑。
 * 作用说明：将业务异常、参数异常和未知异常统一转换为标准 `ApiResponse`，保证接口返回格式一致。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 函数用途：处理业务异常。
     *
     * @param exception 业务异常对象
     * @return 标准化错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException exception) {
        HttpStatus status = mapStatus(exception.getResultCode());
        return ResponseEntity.status(status)
                .body(Result.error(exception.getResultCode(), exception.getMessage()));
    }

    /**
     * 函数用途：处理请求体参数校验失败异常。
     *
     * @param exception 参数校验异常对象
     * @return 标准化错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.INVALID_ARGUMENT.getMessage() : fieldError.getDefaultMessage();
        return ResponseEntity.unprocessableEntity()
                .body(Result.error(ResultCode.INVALID_ARGUMENT, message));
    }

    /**
     * 函数用途：处理路径参数、查询参数等约束校验失败异常。
     *
     * @param exception 约束校验异常对象
     * @return 标准化错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.unprocessableEntity()
                .body(Result.error(ResultCode.INVALID_ARGUMENT, exception.getMessage()));
    }

    /**
     * 函数用途：兜底处理所有未被显式捕获的异常。
     *
     * @param exception 未知异常对象
     * @return 标准化错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.INTERNAL_ERROR, exception.getMessage()));
    }

    /**
     * 函数用途：根据统一业务码映射 HTTP 状态码。
     *
     * @param resultCode 统一业务码
     * @return 对应的 HTTP 状态码
     */
    private HttpStatus mapStatus(ResultCode resultCode) {
        return switch (resultCode) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case INVALID_ARGUMENT -> HttpStatus.UNPROCESSABLE_ENTITY;
            case NOT_IMPLEMENTED -> HttpStatus.NOT_IMPLEMENTED;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
