package com.learnincode.content.interceptor;


import com.learnincode.base.exception.BusinessException;
import com.learnincode.base.exception.CommonError;
import com.learnincode.base.exception.RestErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;



/**
 * @author CalmKin
 * @description 捕获异常之后，转化成RestErrorResponse返回给前端
 * @version 1.0
 * @date 2024/1/18 21:40
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse businessException(BusinessException ex)
    {
        log.error("业务异常{}",ex.getErrMessage());
        return new RestErrorResponse(ex.getErrMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse unknownException(Exception ex)
    {
        log.error("系统异常{}",ex.getMessage());
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }



}
