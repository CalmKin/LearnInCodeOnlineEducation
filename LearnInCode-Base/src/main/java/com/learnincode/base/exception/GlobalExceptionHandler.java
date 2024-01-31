package com.learnincode.base.exception;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;


/**
 * @author CalmKin
 * @description 捕获异常之后，转化成RestErrorResponse返回给前端
 * @version 1.0
 * @date 2024/1/18 21:40
 */
@Slf4j
@RestControllerAdvice
@Configuration
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse businessException(BusinessException ex)
    {
        log.error("业务异常{}",ex.getErrMessage());
        return new RestErrorResponse(ex.getErrMessage());
    }


    /**
     * @author CalmKin
     * @description 捕获JSR303校验抛出的异常
     * @version 1.0
     * @date 2024/1/19 9:18
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse validationException(MethodArgumentNotValidException e)
    {
        BindingResult bindingResult = e.getBindingResult();

        // 因为可能有多个异常信息，所以需要存进数组里面拼接
        List<String> errMsg = new ArrayList<>();

        bindingResult.getFieldErrors().stream().forEach(
                item->{
                    errMsg.add(item.getDefaultMessage());
                }
        );

        // 拼接错误信息
        String join = StringUtils.join(errMsg, ",");

        log.error("【系统异常】{}",join);

        return new RestErrorResponse(join);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse unknownException(Exception ex)
    {
        log.error("系统异常{}",ex.getMessage());
        ex.printStackTrace();
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }



}
