package com.example.order.processing;

import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.model.Product;
import com.example.order.model.ProductCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

public final class OrderFilters {
    private OrderFilters() {
    }

    public static <T extends Product> Predicate<Order<T>> byStatus(OrderStatus status) {
        Objects.requireNonNull(status, "status");
        return order -> order.getStatus() == status;
    }

    public static <T extends Product> Predicate<Order<T>> byCategory(ProductCategory category) {
        Objects.requireNonNull(category, "category");
        return order -> order.getProduct().category() == category;
    }

    public static <T extends Product> Predicate<Order<T>> byProductType(Class<? extends Product> productType) {
        Objects.requireNonNull(productType, "productType");
        return order -> productType.isInstance(order.getProduct());
    }

    public static <T extends Product> Predicate<Order<T>> minTotal(BigDecimal minimum) {
        Objects.requireNonNull(minimum, "minimum");
        return order -> order.totalPrice().compareTo(minimum) >= 0;
    }

    public static <T extends Product> Predicate<Order<T>> customerNameContains(String fragment) {
        Objects.requireNonNull(fragment, "fragment");
        String normalized = fragment.toLowerCase(Locale.ROOT);
        return order -> order.getCustomerName().toLowerCase(Locale.ROOT).contains(normalized);
    }

    public static <T extends Product> List<Order<T>> select(List<Order<T>> orders,
                                                            Predicate<Order<T>> predicate) {
        Objects.requireNonNull(orders, "orders");
        Objects.requireNonNull(predicate, "predicate");
        return orders.stream()
                .filter(predicate)
                .toList();
    }
}
