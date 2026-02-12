package com.example.exception;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.example.common.ApiResponse;
import com.example.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackages = "com.example")
public class GlobalExceptionHandler {

    private static final Log log = LogFactory.get();

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public Result customError(HttpServletRequest request, CustomException e) {
        return Result.error(e.getMsg());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseBody
    public ApiResponse<Void> badRequest(Exception e) {
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ApiResponse<Void> accessDenied() {
        return ApiResponse.fail("权限不足");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ApiResponse<Void> error(HttpServletRequest request, Exception e) {
        log.error("异常信息：", e);
        return ApiResponse.fail("系统错误");
    }
}
