package com.example.order.processing;

import com.example.order.model.Order;
import com.example.order.model.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record ProcessedOrder<T extends Product>(
        Order<T> order,
        BigDecimal discount,
        BigDecimal finalPrice,
        String threadName,
        Instant processedAt) {

    public ProcessedOrder {
        order = Objects.requireNonNull(order, "order");
        discount = Objects.requireNonNull(discount, "discount");
        finalPrice = Objects.requireNonNull(finalPrice, "finalPrice");
        threadName = Objects.requireNonNull(threadName, "threadName");
        processedAt = Objects.requireNonNull(processedAt, "processedAt");
        if (discount.signum() < 0 || discount.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("discount must be between 0 and 1");
        }
        if (finalPrice.signum() < 0) {
            throw new IllegalArgumentException("finalPrice must be zero or positive");
        }
    }
}
