package com.example.order.processing;

import com.example.order.model.Clothing;
import com.example.order.model.Electronics;
import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.model.Product;

import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public final class FakeOrderGenerator {
    private final Faker faker;

    public FakeOrderGenerator() {
        this(new Faker());
    }

    public FakeOrderGenerator(Faker faker) {
        this.faker = Objects.requireNonNull(faker, "faker");
    }

    public List<Order<Electronics>> electronicsOrders(int count) {
        return generate(count, this::electronicsOrder);
    }

    public List<Order<Clothing>> clothingOrders(int count) {
        return generate(count, this::clothingOrder);
    }

    public Order<Electronics> electronicsOrder() {
        Electronics product = Electronics.builder()
                .id(UUID.randomUUID())
                .name(faker.commerce().productName())
                .price(randomPrice())
                .brand(faker.company().name())
                .model(faker.bothify("Model-####"))
                .warrantyMonths(faker.number().numberBetween(6, 37))
                .build();
        return order(product, faker.number().numberBetween(1, 4));
    }

    public Order<Clothing> clothingOrder() {
        Clothing product = Clothing.builder()
                .id(UUID.randomUUID())
                .name(faker.commerce().productName())
                .price(randomPrice())
                .brand(faker.company().name())
                .size(faker.options().option("XS", "S", "M", "L", "XL"))
                .color(faker.color().name())
                .material(faker.options().option("Бавовна", "Льон", "Вовна", "Поліестер"))
                .build();
        return order(product, faker.number().numberBetween(1, 4));
    }

    private <T extends Product> List<Order<T>> generate(int count, Supplier<Order<T>> supplier) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be zero or positive");
        }
        return IntStream.range(0, count)
                .mapToObj(index -> supplier.get())
                .toList();
    }

    private <T extends Product> Order<T> order(T product, int quantity) {
        return Order.<T>builder()
                .id(UUID.randomUUID())
                .customerName(faker.name().fullName())
                .product(product)
                .quantity(quantity)
                .createdAt(Instant.now())
                .status(OrderStatus.NEW)
                .build();
    }

    private BigDecimal randomPrice() {
        return BigDecimal.valueOf(faker.number().randomDouble(2, 100, 5000));
    }
}
