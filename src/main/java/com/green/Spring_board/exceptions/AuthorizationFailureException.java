package com.green.Spring_board.exceptions;

// 누구인지 알지만 (인증은 되었지만) 해당 작업을 허용하지 않음 (403)
public class AuthorizationFailureException extends RuntimeException {
    public AuthorizationFailureException(String message) {
        super(message);
    }
}
