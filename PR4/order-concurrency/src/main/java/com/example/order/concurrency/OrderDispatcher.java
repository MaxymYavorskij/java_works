package com.example.order.concurrency;

import com.example.order.model.Order;
import com.example.order.model.Product;
import com.example.order.processing.OrderProcessor;
import com.example.order.processing.ProcessedOrder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class OrderDispatcher<T extends Product> implements AutoCloseable {
    private final ExecutorService executor;
    private final Duration shutdownTimeout;

    public OrderDispatcher(String threadPrefix, int poolSize) {
        this(threadPrefix, poolSize, Duration.ofSeconds(10));
    }

    public OrderDispatcher(String threadPrefix, int poolSize, Duration shutdownTimeout) {
        Objects.requireNonNull(threadPrefix, "threadPrefix");
        this.shutdownTimeout = Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be greater than zero");
        }

        AtomicInteger threadCounter = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(poolSize, runnable -> {
            Thread thread = new Thread(runnable,
                    threadPrefix + "-worker-" + threadCounter.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        });
    }

    public Future<ProcessedOrder<T>> submit(Order<T> order, OrderProcessor<T> processor) {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(processor, "processor");
        return executor.submit(() -> processor.process(order));
    }

    /** A failure in one order does not stop other orders. */
    public BatchResult<T> processAll(List<Order<T>> orders, OrderProcessor<T> processor) {
        Objects.requireNonNull(orders, "orders");
        Objects.requireNonNull(processor, "processor");

        List<Order<T>> snapshot = List.copyOf(orders);
        List<Callable<ProcessedOrder<T>>> tasks = snapshot.stream()
                .<Callable<ProcessedOrder<T>>>map(order -> () -> processor.process(order))
                .toList();

        List<ProcessedOrder<T>> successful = new ArrayList<>();
        List<FailedOrder<T>> failed = new ArrayList<>();
        long startedAt = System.nanoTime();

        try {
            List<Future<ProcessedOrder<T>>> futures = executor.invokeAll(tasks);
            for (int index = 0; index < futures.size(); index++) {
                try {
                    successful.add(futures.get(index).get());
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    String message = cause.getMessage() == null
                            ? cause.getClass().getSimpleName()
                            : cause.getMessage();
                    failed.add(new FailedOrder<>(snapshot.get(index), message));
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new OrderDispatchException("Interrupted while waiting for orders", ex);
        }

        return new BatchResult<>(successful, failed, Duration.ofNanos(System.nanoTime() - startedAt));
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    throw new OrderDispatchException("Thread pool did not terminate");
                }
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new OrderDispatchException("Interrupted while shutting down thread pool", ex);
        }
    }
}
