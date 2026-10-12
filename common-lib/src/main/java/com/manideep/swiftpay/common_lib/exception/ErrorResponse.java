package com.manideep.swiftpay.common_lib.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String errorCode,
        String errorDescription,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message){}

    public static ErrorResponse of(String errorCode, String errorDescription){
        return new ErrorResponse(errorCode,errorDescription,LocalDateTime.now(),null);
    }

    public static ErrorResponse of(String errorCode, String errorDescription,List<FieldError> fieldErrors){
        return new ErrorResponse(errorCode,errorDescription,LocalDateTime.now(),fieldErrors);
    }
}
