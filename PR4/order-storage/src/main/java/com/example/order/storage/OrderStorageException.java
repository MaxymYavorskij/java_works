package com.example.order.storage;

public class OrderStorageException extends RuntimeException {
    public OrderStorageException(String message) {
        super(message);
    }

    public OrderStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
