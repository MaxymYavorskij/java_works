package com.example.order.concurrency;

import com.example.order.model.Product;
import com.example.order.processing.ProcessedOrder;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public record BatchResult<T extends Product>(
        List<ProcessedOrder<T>> successful,
        List<FailedOrder<T>> failed,
        Duration duration) {

    public BatchResult {
        successful = List.copyOf(Objects.requireNonNull(successful, "successful"));
        failed = List.copyOf(Objects.requireNonNull(failed, "failed"));
        duration = Objects.requireNonNull(duration, "duration");
    }

    public int total() {
        return successful.size() + failed.size();
    }
}
