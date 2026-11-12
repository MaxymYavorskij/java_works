package com.example.order.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class Clothing extends Product {
    private final String brand;
    private final String size;
    private final String color;
    private final String material;

    @Builder
    public Clothing(UUID id, String name, BigDecimal price, String brand, String size, String color, String material) {
        super(id, name, price);
        this.brand = ModelValidation.requireText(brand, "brand");
        this.size = ModelValidation.requireText(size, "size");
        this.color = ModelValidation.requireText(color, "color");
        this.material = ModelValidation.requireText(material, "material");
    }

    @Override
    public ProductCategory category() {
        return ProductCategory.CLOTHING;
    }
}
