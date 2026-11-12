package com.example.order.processing.internal;

import com.example.order.processing.OrderProcessingException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Internal price calculation. The package is not exported. */
public final class PriceCalculator {
    private PriceCalculator() {
    }

    public static BigDecimal applyDiscount(BigDecimal total, BigDecimal discount) {
        if (total == null || total.signum() < 0) {
            throw new OrderProcessingException("total must be zero or positive");
        }
        if (discount == null || discount.signum() < 0 || discount.compareTo(BigDecimal.ONE) > 0) {
            throw new OrderProcessingException("discount must be between 0 and 1");
        }
        return total.subtract(total.multiply(discount)).setScale(2, RoundingMode.HALF_UP);
    }
}
