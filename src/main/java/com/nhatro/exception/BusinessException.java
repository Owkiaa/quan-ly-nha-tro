package com.nhatro.exception;

/**
 * Ngoại lệ nghiệp vụ chung của hệ thống.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
