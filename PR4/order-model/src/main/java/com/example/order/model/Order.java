package com.example.order.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class Order<T extends Product> {
    private final UUID id;
    private final String customerName;
    private final T product;
    private final int quantity;
    private final Instant createdAt;
    private final OrderStatus status;

    @Builder
    public Order(UUID id,
                 String customerName,
                 T product,
                 int quantity,
                 Instant createdAt,
                 OrderStatus status) {
        this.id = ModelValidation.requireNonNull(id, "id");
        this.customerName = ModelValidation.requireText(customerName, "customerName");
        this.product = ModelValidation.requireNonNull(product, "product");
        this.quantity = ModelValidation.requirePositive(quantity, "quantity");
        this.createdAt = ModelValidation.requireNonNull(createdAt, "createdAt");
        this.status = ModelValidation.requireNonNull(status, "status");
    }

    public BigDecimal totalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Order<T> withStatus(OrderStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus");
        return new Order<>(id, customerName, product, quantity, createdAt, newStatus);
    }
}
