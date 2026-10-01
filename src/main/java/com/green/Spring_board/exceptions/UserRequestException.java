package com.green.Spring_board.exceptions;

public class UserRequestException extends RuntimeException {
    public UserRequestException(String message) {
        super(message);
    }
}
