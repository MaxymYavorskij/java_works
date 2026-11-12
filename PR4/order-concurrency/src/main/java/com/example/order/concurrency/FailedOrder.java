package com.example.order.concurrency;

import com.example.order.model.Order;
import com.example.order.model.Product;

import java.util.Objects;

public record FailedOrder<T extends Product>(Order<T> order, String errorMessage) {
    public FailedOrder {
        order = Objects.requireNonNull(order, "order");
        errorMessage = Objects.requireNonNull(errorMessage, "errorMessage");
    }
}
