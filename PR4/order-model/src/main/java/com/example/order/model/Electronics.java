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
public final class Electronics extends Product {
    private final String brand;
    private final String model;
    private final int warrantyMonths;

    @Builder
    public Electronics(UUID id, String name, BigDecimal price, String brand, String model, int warrantyMonths) {
        super(id, name, price);
        this.brand = ModelValidation.requireText(brand, "brand");
        this.model = ModelValidation.requireText(model, "model");
        if (warrantyMonths < 0) {
            throw new OrderValidationException("warrantyMonths must be zero or positive");
        }
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public ProductCategory category() {
        return ProductCategory.ELECTRONICS;
    }
}
