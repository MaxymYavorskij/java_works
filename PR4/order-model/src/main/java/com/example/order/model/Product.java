package com.example.order.model;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class Product {
    private final UUID id;
    private final String name;
    private final BigDecimal price;

    protected Product(UUID id, String name, BigDecimal price) {
        this.id = ModelValidation.requireNonNull(id, "id");
        this.name = ModelValidation.requireText(name, "name");
        this.price = ModelValidation.requireNonNegative(price, "price");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public abstract ProductCategory category();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Product product)) {
            return false;
        }
        return id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", name='" + name + "', price=" + price + '}';
    }
}
