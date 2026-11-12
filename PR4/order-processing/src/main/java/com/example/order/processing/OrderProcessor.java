package com.example.order.processing;

import com.example.order.model.Order;
import com.example.order.model.Product;

@FunctionalInterface
public interface OrderProcessor<T extends Product> {
    ProcessedOrder<T> process(Order<T> order);
}
