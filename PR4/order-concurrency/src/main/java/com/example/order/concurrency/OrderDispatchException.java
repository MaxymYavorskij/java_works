package com.example.order.concurrency;

public class OrderDispatchException extends RuntimeException {
    public OrderDispatchException(String message) {
        super(message);
    }

    public OrderDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
