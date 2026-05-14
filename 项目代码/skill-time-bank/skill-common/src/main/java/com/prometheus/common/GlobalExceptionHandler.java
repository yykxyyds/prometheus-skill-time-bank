package com.prometheus.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.fail(400, "请求格式不正确，请检查参数");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {} = {}", e.getName(), e.getValue());
        return Result.fail(400, "参数类型不正确: " + e.getName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return Result.fail(400, "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(NumberFormatException.class)
    public Result<?> handleNumberFormat(NumberFormatException e) {
        log.warn("数字格式错误: {}", e.getMessage());
        return Result.fail(400, "数字格式不正确");
    }

    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointer(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.fail(500, "服务器内部错误");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
