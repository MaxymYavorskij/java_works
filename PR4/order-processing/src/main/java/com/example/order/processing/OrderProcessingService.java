package com.example.order.processing;

import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.model.Product;
import com.example.order.processing.internal.PriceCalculator;
import com.example.order.storage.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class OrderProcessingService<T extends Product> implements OrderProcessor<T> {
    private final Class<T> productType;
    private final OrderRepository<T> repository;
    private final DiscountPolicy discountPolicy;

    public OrderProcessingService(Class<T> productType,
                                  OrderRepository<T> repository,
                                  DiscountPolicy discountPolicy) {
        this.productType = Objects.requireNonNull(productType, "productType");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "discountPolicy");
        if (!repository.productType().equals(productType)) {
            throw new IllegalArgumentException("Repository type " + repository.productType().getName()
                    + " does not match processor type " + productType.getName());
        }
    }

    @Override
    public ProcessedOrder<T> process(Order<T> order) {
        Objects.requireNonNull(order, "order");
        if (!productType.isInstance(order.getProduct())) {
            throw new UnsupportedOrderTypeException(productType, order.getProduct().getClass());
        }

        try {
            BigDecimal discount = Objects.requireNonNull(
                    discountPolicy.discountFor(order), "discountPolicy returned null");
            BigDecimal finalPrice = PriceCalculator.applyDiscount(order.totalPrice(), discount);
            Order<T> completed = order.withStatus(OrderStatus.COMPLETED);
            repository.save(completed);

            return new ProcessedOrder<>(
                    completed,
                    discount,
                    finalPrice,
                    Thread.currentThread().getName(),
                    Instant.now());
        } catch (RuntimeException ex) {
            repository.save(order.withStatus(OrderStatus.FAILED));
            throw new OrderProcessingException(
                    "Failed to process order " + order.getId() + ": " + ex.getMessage(), ex);
        }
    }

    public List<ProcessedOrder<T>> processAll(List<Order<T>> orders) {
        Objects.requireNonNull(orders, "orders");
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.NEW)
                .map(this::process)
                .toList();
    }

    public OrderRepository<T> repository() {
        return repository;
    }
}
