package com.prometheus.common;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletResponse response) {
        log.warn("业务异常: {}", e.getMessage());
        if (e.getCode() == 401) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletResponse response) {
        log.warn("不支持的请求方法: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return Result.fail(405, "不支持的请求方法: " + e.getMethod());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("数据完整性异常: {}", e.getMessage());
        return Result.fail(400, "数据校验失败，请检查必填字段");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFound(NoResourceFoundException e, HttpServletResponse response) {
        log.warn("资源未找到: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return Result.fail(404, "接口不存在: " + e.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
