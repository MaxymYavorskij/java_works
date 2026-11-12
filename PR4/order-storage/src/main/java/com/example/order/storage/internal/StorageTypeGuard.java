package com.example.order.storage.internal;

import com.example.order.model.Order;
import com.example.order.model.Product;
import com.example.order.storage.OrderStorageException;

/** Internal guard. The package is not exported. */
public final class StorageTypeGuard {
    private StorageTypeGuard() {
    }

    public static <T extends Product> void requireType(Class<T> expectedType, Order<T> order) {
        T product = order.getProduct();
        if (!expectedType.isInstance(product)) {
            throw new OrderStorageException("Order " + order.getId()
                    + " contains " + product.getClass().getSimpleName()
                    + " but repository stores " + expectedType.getSimpleName());
        }
    }
}
