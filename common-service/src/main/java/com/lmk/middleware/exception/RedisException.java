package com.lmk.middleware.exception;

public class RedisException extends RuntimeException{
    public RedisException() {
    }

    public RedisException(String message) {
        super(message);
    }
}
