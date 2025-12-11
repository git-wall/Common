//package org.app.common.design.legacy;
//
//import lombok.Getter;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Objects;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
///**
// * Example class demonstrating how to use the Stage interface with the fluent pipeline pattern.
// */
//public class StageExample {
//
//    /**
//     * Example Order class for demonstration.
//     */
//    @Getter
//    public static class Order {
//        private final String id;
//        private final double amount;
//        private final String status;
//        private final String customerType;
//
//        public Order(String id, double amount, String status, String customerType) {
//            this.id = id;
//            this.amount = amount;
//            this.status = status;
//            this.customerType = customerType;
//        }
//
//        @Override
//        public String toString() {
//            return "Order{" +
//                    "id='" + id + '\'' +
//                    ", amount=" + amount +
//                    ", status='" + status + '\'' +
//                    ", customerType='" + customerType + '\'' +
//                    '}';
//        }
//    }
//
//    /**
//     * Example ProcessedOrder class for demonstration.
//     */
//    @Getter
//    public static class ProcessedOrder {
//        private final Order order;
//        private final double discountedAmount;
//        private final double tax;
//        private final double finalAmount;
//
//        public ProcessedOrder(Order order, double discountedAmount, double tax, double finalAmount) {
//            this.order = order;
//            this.discountedAmount = discountedAmount;
//            this.tax = tax;
//            this.finalAmount = finalAmount;
//        }
//
//        @Override
//        public String toString() {
//            return "ProcessedOrder{" +
//                    "order=" + order +
//                    ", discountedAmount=" + discountedAmount +
//                    ", tax=" + tax +
//                    ", finalAmount=" + finalAmount +
//                    '}';
//        }
//    }
//
//    /**
//     * Example of using the Stage interface to process orders through a pipeline.
//     */
//    public static void main(String[] args) {
//        // Create a list of orders
//        List<Order> orders = Arrays.asList(
//                new Order("ORD-001", 100.0, "NEW", "REGULAR"),
//                new Order("ORD-002", 200.0, "NEW", "PREMIUM"),
//                new Order("ORD-003", 150.0, "PROCESSING", "REGULAR"),
//                new Order("ORD-004", 300.0, "COMPLETED", "PREMIUM"),
//                new Order("ORD-005", 50.0, "CANCELLED", "REGULAR")
//        );
//
//        // Create stages for order processing
//        Stage<Order, Order> validateOrder = order -> {
//            if (order.getStatus().equals("CANCELLED")) {
//                throw new IllegalStateException("Cannot process cancelled order: " + order.getId());
//            }
//            return order;
//        };
//
//        Stage<Order, Order> applyDiscount = order -> {
//            System.out.println("Applying discount to order: " + order.getId());
//            return order;
//        };
//
//        Function<Order, Double> calculateDiscount = order -> {
//            if (order.getCustomerType().equals("PREMIUM")) {
//                return order.getAmount() * 0.1; // 10% discount for premium customers
//            }
//            return order.getAmount() * 0.05; // 5% discount for regular customers
//        };
//
//        Stage<Order, ProcessedOrder> processOrder = order -> {
//            double discount = calculateDiscount.apply(order);
//            double discountedAmount = order.getAmount() - discount;
//            double tax = discountedAmount * 0.08; // 8% tax
//            double finalAmount = discountedAmount + tax;
//            return new ProcessedOrder(order, discountedAmount, tax, finalAmount);
//        };
//
//        // Create a complete order processing pipeline
//        Stage<Order, ProcessedOrder> orderProcessingPipeline = validateOrder
//                .handleError(e -> {
//                    System.err.println("Error: " + e.getMessage());
//                    return null; // Return null for failed orders
//                })
//                .then(applyDiscount)
//                .peek(order -> System.out.println("Processing order: " + order.getId()))
//                .then(processOrder);
//
//        // Process all orders through the pipeline
//        System.out.println("Processing orders through the pipeline:");
//        List<ProcessedOrder> processedOrders = orders.stream()
//                .map(orderProcessingPipeline)
//                .filter(Objects::nonNull) // Filter out failed orders
//                .collect(Collectors.toList());
//
//        // Print the processed orders
//        System.out.println("\nProcessed orders:");
//        processedOrders.forEach(System.out::println);
//
//        // Example of conditional processing
//        Stage<Order, String> orderStatusStage = Stage.of(Order::getStatus);
//        Stage<Order, String> conditionalProcessing = Stage.of(Order::getStatus)
//                .when(
//                    status -> status.equals("NEW"),
//                    status -> "Order is new and needs processing",
//                    status -> "Order is already being processed or completed"
//                );
//
//        System.out.println("\nConditional processing results:");
//        orders.forEach(order -> {
//            String result = conditionalProcessing.process(order);
//            System.out.println(order.getId() + ": " + result);
//        });
//
//        // Example of using the conditional static factory method
//        Stage<Order, Order> premiumCustomerStage = Stage.conditional(
//                order -> order.getCustomerType().equals("PREMIUM"),
//                order -> {
//                    System.out.println("Special handling for premium customer order: " + order.getId());
//                    return order;
//                }
//        );
//
//        System.out.println("\nPremium customer handling:");
//        orders.forEach(premiumCustomerStage::process);
//    }
//}
