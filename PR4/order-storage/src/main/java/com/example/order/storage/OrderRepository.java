package com.example.order.storage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Comparator;

import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.model.Product;
import com.example.order.storage.internal.StorageTypeGuard;

/** Thread-safe in-memory repository. */
public final class OrderRepository<T extends Product> {
    private final Class<T> productType;
    private final ConcurrentMap<UUID, Order<T>> orders = new ConcurrentHashMap<>();

    public OrderRepository(Class<T> productType) {
        this.productType = Objects.requireNonNull(productType, "productType");
    }

    public Class<T> productType() {
        return productType;
    }

    public void save(Order<T> order) {
        Objects.requireNonNull(order, "order");
        StorageTypeGuard.requireType(productType, order);
        orders.put(order.getId(), order);
    }

    public Optional<Order<T>> findById(UUID id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(orders.get(id));
    }

    public List<Order<T>> findAll() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .toList();
    }

    public List<Order<T>> findByStatus(OrderStatus status) {
        Objects.requireNonNull(status, "status");
        return orders.values().stream()
                .filter(order -> order.getStatus() == status)
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .toList();
    }

    public boolean remove(UUID id) {
        Objects.requireNonNull(id, "id");
        return orders.remove(id) != null;
    }

    public int size() {
        return orders.size();
    }

    public void clear() {
        orders.clear();
    }
}
