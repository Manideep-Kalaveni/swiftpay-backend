package com.manideep.swiftpay.common_lib.exception;

import lombok.Getter;

@Getter
public class InvalidStateTransitionException extends RuntimeException{
    private final String fromState;
    private final String event;

    public InvalidStateTransitionException(String fromState, String event) {
        super("Invalid transition from %s with event %s".formatted(fromState,event));
        this.fromState = fromState;
        this.event = event;
    }
}
