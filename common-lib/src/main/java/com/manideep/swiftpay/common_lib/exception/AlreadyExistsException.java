package com.manideep.swiftpay.common_lib.exception;

import lombok.Getter;

@Getter
public class AlreadyExistsException extends RuntimeException{

    private final String errorCode;

    public AlreadyExistsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
