package com.learnincode.base.exception;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException{
    private String errMessage;

    public BusinessException(String message) {
        super(message);
        this.errMessage = message;
    }
}
