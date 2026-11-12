package com.example.order.processing;

import com.example.order.model.Order;
import com.example.order.model.Product;
import com.example.order.model.ProductCategory;

import java.math.BigDecimal;
import java.util.Map;

/** Returns a discount rate in the range {@code [0, 1]}. */
@FunctionalInterface
public interface DiscountPolicy {
    BigDecimal discountFor(Order<? extends Product> order);

    static DiscountPolicy none() {
        return order -> BigDecimal.ZERO;
    }

    static DiscountPolicy quantityBased() {
        return order -> order.getQuantity() >= 3
                ? new BigDecimal("0.10")
                : BigDecimal.ZERO;
    }

    static DiscountPolicy categoryBased(Map<ProductCategory, BigDecimal> rates) {
        Map<ProductCategory, BigDecimal> copy = Map.copyOf(rates);
        return order -> copy.getOrDefault(order.getProduct().category(), BigDecimal.ZERO);
    }
}
