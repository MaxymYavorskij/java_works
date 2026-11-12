package com.example.order.model;

import java.math.BigDecimal;
import java.util.Objects;

final class ModelValidation {
    private ModelValidation() {
    }

    static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new OrderValidationException(field + " must not be null");
        }
        return value;
    }

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new OrderValidationException(field + " must not be blank");
        }
        return value;
    }

    static BigDecimal requireNonNegative(BigDecimal value, String field) {
        Objects.requireNonNull(value, field);
        if (value.signum() < 0) {
            throw new OrderValidationException(field + " must be zero or positive");
        }
        return value;
    }

    static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new OrderValidationException(field + " must be greater than zero");
        }
        return value;
    }
}
