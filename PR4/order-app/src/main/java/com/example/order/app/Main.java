package com.example.order.app;

import com.example.order.concurrency.BatchResult;
import com.example.order.concurrency.OrderDispatcher;
import com.example.order.model.Clothing;
import com.example.order.model.Electronics;
import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.model.OrderValidationException;
import com.example.order.model.Product;
import com.example.order.model.ProductCategory;
import com.example.order.processing.DiscountPolicy;
import com.example.order.processing.FakeOrderGenerator;
import com.example.order.processing.OrderFilters;
import com.example.order.processing.OrderProcessingException;
import com.example.order.processing.OrderProcessingService;
import com.example.order.processing.ProcessedOrder;
import com.example.order.storage.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Main {
    public static void main(String[] args) {
        System.out.println("=== Багатопотокова система обробки замовлень ===");

        OrderRepository<Electronics> electronicsRepository = new OrderRepository<>(Electronics.class);
        OrderRepository<Clothing> clothingRepository = new OrderRepository<>(Clothing.class);

        OrderProcessingService<Electronics> electronicsService = new OrderProcessingService<>(
                Electronics.class,
                electronicsRepository,
                DiscountPolicy.quantityBased());
        OrderProcessingService<Clothing> clothingService = new OrderProcessingService<>(
                Clothing.class,
                clothingRepository,
                DiscountPolicy.categoryBased(Map.of(ProductCategory.CLOTHING, new BigDecimal("0.05"))));

        FakeOrderGenerator generator = new FakeOrderGenerator();
        List<Order<Electronics>> electronicsOrders = generator.electronicsOrders(8);
        List<Order<Clothing>> clothingOrders = generator.clothingOrders(6);

        List<Order<Electronics>> premiumElectronics = OrderFilters.select(
                electronicsOrders,
                OrderFilters.<Electronics>byProductType(Electronics.class)
                        .and(OrderFilters.minTotal(new BigDecimal("1000"))));

        System.out.printf("Згенеровано: електроніка=%d, одяг=%d%n",
                electronicsOrders.size(), clothingOrders.size());
        System.out.printf("Фільтр (електроніка від 1000 грн): %d замовлень%n",
                premiumElectronics.size());

        BatchResult<Electronics> electronicsResult;
        BatchResult<Clothing> clothingResult;

        try (OrderDispatcher<Electronics> electronicsDispatcher = new OrderDispatcher<>("electronics", 4);
             OrderDispatcher<Clothing> clothingDispatcher = new OrderDispatcher<>("clothing", 3)) {

            electronicsResult = electronicsDispatcher.processAll(electronicsOrders, electronicsService);
            clothingResult = clothingDispatcher.processAll(clothingOrders, clothingService);
        }

        printBatch("Електроніка", electronicsResult);
        printBatch("Одяг", clothingResult);

        System.out.printf("%nУ сховищі: електроніка=%d, одяг=%d%n",
                electronicsRepository.size(), clothingRepository.size());

        System.out.println("\nЗавершені замовлення зі сховища:");
        electronicsRepository.findByStatus(OrderStatus.COMPLETED).forEach(Main::printOrder);
        clothingRepository.findByStatus(OrderStatus.COMPLETED).forEach(Main::printOrder);

        demonstrateProcessingFailure(generator, clothingRepository);
        demonstrateValidationFailure(generator);
    }

    private static <T extends Product> void printBatch(String title, BatchResult<T> result) {
        System.out.printf("%n[%s] успішно=%d, помилок=%d, час=%d мс%n",
                title,
                result.successful().size(),
                result.failed().size(),
                result.duration().toMillis());

        result.successful().stream()
                .map(ProcessedOrder::order)
                .forEach(Main::printOrder);

        result.failed().forEach(failure ->
                System.out.println("  ! " + failure.errorMessage()));
    }

    private static void printOrder(Order<? extends Product> order) {
        System.out.printf("  %-8s | %-12s | %-24s | x%d | %9.2f грн | %s%n",
                order.getId().toString().substring(0, 8),
                order.getProduct().category().getTitle(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.totalPrice(),
                order.getStatus());
    }

    private static void demonstrateProcessingFailure(FakeOrderGenerator generator,
                                                     OrderRepository<Clothing> repository) {
        OrderProcessingService<Clothing> invalidDiscountService = new OrderProcessingService<>(
                Clothing.class,
                repository,
                order -> new BigDecimal("1.5"));
        Order<Clothing> sample = generator.clothingOrder();

        try {
            invalidDiscountService.process(sample);
        } catch (OrderProcessingException ex) {
            System.out.println("\nПерехоплено OrderProcessingException: " + ex.getMessage());
            String cause = ex.getCause() == null ? "немає" : ex.getCause().getMessage();
            System.out.println("Причина: " + cause);
        }
    }

    private static void demonstrateValidationFailure(FakeOrderGenerator generator) {
        Clothing product = generator.clothingOrder().getProduct();

        try {
            Order.<Clothing>builder()
                    .id(UUID.randomUUID())
                    .customerName(" ")
                    .product(product)
                    .quantity(1)
                    .createdAt(Instant.now())
                    .status(OrderStatus.NEW)
                    .build();
        } catch (OrderValidationException ex) {
            System.out.println("Перехоплено OrderValidationException: " + ex.getMessage());
        }
    }
}
