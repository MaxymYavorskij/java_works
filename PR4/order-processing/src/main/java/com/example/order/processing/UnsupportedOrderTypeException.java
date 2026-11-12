package com.example.order.processing;

import com.example.order.model.Product;

public class UnsupportedOrderTypeException extends OrderProcessingException {
    private final Class<? extends Product> expectedType;
    private final Class<? extends Product> actualType;

    public UnsupportedOrderTypeException(Class<? extends Product> expectedType,
                                         Class<? extends Product> actualType) {
        super("Expected product type " + expectedType.getName()
                + " but received " + actualType.getName());
        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    public Class<? extends Product> getExpectedType() {
        return expectedType;
    }

    public Class<? extends Product> getActualType() {
        return actualType;
    }
}
