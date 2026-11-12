package com.example.order.model;

public class OrderValidationException extends IllegalArgumentException {
    public OrderValidationException(String message) {
        super(message);
    }

    public OrderValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
