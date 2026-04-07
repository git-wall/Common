//package org.app.core.action;
//
//import java.io.ByteArrayInputStream;
//import java.nio.file.Path;
//import java.util.logging.Logger;
//
///**
// * Ví dụ thực tế: Order Processing System
// *
// * Case 1 — Normal Pipeline:
// *   POST /orders → validate → check inventory API → save DB → notify customer
// *   Trả về HTTP response ngay, notify chạy background
// *
// * Case 2 — Stream Pipeline:
// *   POST /orders/bulk-upload (CSV 500MB) → parse → validate → save to DB → push progress qua SSE
// *
// * Case 3 — Normal Pipeline với fallback:
// *   GET /product-price → gọi pricing API → nếu fail thì dùng cached price
// */
//public class OrderProcessingExample {
//
//    private static final Logger log = Logger.getLogger(OrderProcessingExample.class.getName());
//
//    // ─── Fake domain types ────────────────────────────────────────────────────
//
//    record OrderRequest(String customerId, String productId, int quantity) {}
//    record ValidatedOrder(OrderRequest req, double price) {}
//    record SavedOrder(String orderId, ValidatedOrder order) {}
//
//    enum AppError {
//        INVALID_REQUEST,
//        PRODUCT_NOT_FOUND,
//        INSUFFICIENT_STOCK,
//        PAYMENT_FAILED,
//        DB_ERROR,
//        API_TIMEOUT
//    }
//
//    // ─── Case 1: Normal Order Flow ────────────────────────────────────────────
//
//    /**
//     * Điểm mấu chốt:
//     * - validate + checkInventory + save: blocking (cần kết quả)
//     * - notify: async fire & forget (user không cần đợi email/push)
//     * - logError: blocking (cần log trước khi trả lỗi về caller)
//     * - alertOps: async (Slack alert không cần block HTTP response)
//     *
//     * HTTP handler nhận Result → trả 200 hay 4xx/5xx tùy Ok/Err.
//     */
//    public Result<SavedOrder, AppError> processOrder(OrderRequest request) {
//        return NormalPipeline
//            .from(() -> validateOrder(request))             // Step 1: validate input
//            .flatMap(this::checkInventory)                  // Step 2: gọi inventory API
//            .flatMap(this::processPayment)                  // Step 3: charge
//            .flatMap(this::saveToDatabase)                  // Step 4: persist
//
//            // === Fan-out handlers ===
//            .onSuccessAsync(order -> sendConfirmationEmail(order))   // background: email
//            .onSuccessAsync(order -> pushNotification(order))        // background: push
//            .onSuccessAsync(order -> updateAnalytics(order))         // background: analytics
//
//            .onFailure(err -> logStructured(err, request))           // blocking: log trước
//            .onFailureAsync(err -> alertOpsTeam(err, request))       // background: Slack alert
//
//            .peek(result -> recordMetric("order.process", result.isOk()))  // luôn chạy
//
//            .execute();
//    }
//
//    // ─── Case 2: Bulk CSV Upload ──────────────────────────────────────────────
//
//    /**
//     * Upload 500MB CSV → parse từng dòng → validate → save batch → progress SSE.
//     *
//     * Key points:
//     * - KHÔNG load hết CSV vào memory
//     * - Mỗi dòng được parse, validate, rồi ghi ra output file (hoặc batch-insert DB)
//     * - Progress push qua SSE cho FE cập nhật loading bar
//     * - onComplete → notify admin
//     */
//    public Result<StreamPipeline.StreamSummary, StreamPipeline.StreamError> processBulkUpload(
//        byte[] csvBytes,
//        String uploadId,
//        java.io.OutputStream sseStream // Spring response.getOutputStream()
//    ) throws Exception {
//        return StreamPipeline
//            .fromLines(new ByteArrayInputStream(csvBytes))
//            .filter(line -> !line.isBlank() && !line.startsWith("#"))  // skip blank/comment
//            .map(line -> parseCsvLine(line))                           // parse → OrderRequest serialized
//            .sink(StreamPipeline.Sinks.toTextFile(
//                Path.of("/tmp/bulk-orders-" + uploadId + ".csv")
//            ))
//            .onProgress(progress -> {
//                // Push SSE: "data: {\"done\": 42, \"pct\": 67.3}\n\n"
//                String event = "data: {\"chunks\":%d, \"pct\":%.1f}\n\n"
//                    .formatted(progress.chunksProcessed(), progress.percentDone());
//                try { sseStream.write(event.getBytes()); sseStream.flush(); }
//                catch (Exception ignored) {}
//            })
//            .onComplete(summary -> {
//                log.info("Bulk upload done: %d bytes, %dms → %s"
//                    .formatted(summary.totalBytes(), summary.durationMs(), summary.destination()));
//                notifyAdminBulkDone(uploadId, summary);
//            })
//            .onError(err -> {
//                log.severe("Bulk upload failed at byte %d: %s"
//                    .formatted(err.bytesBeforeError(), err.message()));
//            })
//            .execute();
//    }
//
//    // ─── Case 3: Price lookup với fallback ───────────────────────────────────
//
//    /**
//     * Gọi pricing service → nếu timeout/fail → dùng cached price.
//     * Common pattern: không muốn hard fail chỉ vì pricing service down.
//     */
//    public Result<Double, AppError> getProductPrice(String productId) {
//        return NormalPipeline
//            .from(() -> callPricingApi(productId))          // thử gọi API
//            .fallback(() -> getCachedPrice(productId))      // nếu fail → fallback
//            .onFailure(err -> log.warning("Price lookup failed entirely for " + productId))
//            .execute();
//    }
//
//    // ─── Case 4: Compose pipelines ───────────────────────────────────────────
//
//    /**
//     * Kết hợp: lấy price trước, rồi dùng trong processOrder.
//     * Đây là cách chain 2 pipeline với nhau.
//     */
//    public Result<SavedOrder, AppError> processOrderWithDynamicPrice(OrderRequest request) {
//        // Lấy price
//        Result<Double, AppError> priceResult = getProductPrice(request.productId());
//
//        if (priceResult.isErr()) {
//            return Result.err(priceResult.error().orElseThrow());
//        }
//
//        double price = priceResult.getOrThrow();
//
//        // Tiếp tục với price đã lấy được
//        return NormalPipeline
//            .of(Result.<ValidatedOrder, AppError>ok(
//                new ValidatedOrder(request, price)
//            ))
//            .flatMap(this::processPayment)
//            .flatMap(this::saveToDatabase)
//            .onSuccessAsync(order -> sendConfirmationEmail(order))
//            .onFailure(err -> logStructured(err, request))
//            .execute();
//    }
//
//    // ─── Fake implementations (thay bằng thật trong production) ──────────────
//
//    private Result<ValidatedOrder, AppError> validateOrder(OrderRequest req) {
//        if (req.quantity() <= 0) return Result.err(AppError.INVALID_REQUEST);
//        if (req.customerId() == null) return Result.err(AppError.INVALID_REQUEST);
//        return Result.ok(new ValidatedOrder(req, 99.99));
//    }
//
//    private Result<ValidatedOrder, AppError> checkInventory(ValidatedOrder order) {
//        // Gọi inventory service, check stock
//        boolean inStock = true; // fake
//        return inStock ? Result.ok(order) : Result.err(AppError.INSUFFICIENT_STOCK);
//    }
//
//    private Result<ValidatedOrder, AppError> processPayment(ValidatedOrder order) {
//        // Gọi payment gateway
//        return Result.ok(order); // fake success
//    }
//
//    private Result<SavedOrder, AppError> saveToDatabase(ValidatedOrder order) {
//        try {
//            String orderId = "ORD-" + System.currentTimeMillis();
//            return Result.ok(new SavedOrder(orderId, order));
//        } catch (Exception e) {
//            return Result.err(AppError.DB_ERROR);
//        }
//    }
//
//    private Result<Double, AppError> callPricingApi(String productId) {
//        // Simulate timeout
//        return Result.err(AppError.API_TIMEOUT);
//    }
//
//    private Result<Double, AppError> getCachedPrice(String productId) {
//        return Result.ok(89.99); // cache hit
//    }
//
//    private String parseCsvLine(String line) {
//        return line.toUpperCase(); // fake parse
//    }
//
//    private void sendConfirmationEmail(SavedOrder order) {
//        log.info("[async] Email sent for " + order.orderId());
//    }
//
//    private void pushNotification(SavedOrder order) {
//        log.info("[async] Push sent for " + order.orderId());
//    }
//
//    private void updateAnalytics(SavedOrder order) {
//        log.info("[async] Analytics updated for " + order.orderId());
//    }
//
//    private void logStructured(AppError err, OrderRequest req) {
//        log.severe("[error] " + err + " for customer " + req.customerId());
//    }
//
//    private void alertOpsTeam(AppError err, OrderRequest req) {
//        log.warning("[async] Slack alert: " + err);
//    }
//
//    private void recordMetric(String name, boolean success) {
//        log.info("[metric] " + name + " = " + (success ? "ok" : "err"));
//    }
//
//    private void notifyAdminBulkDone(String uploadId, StreamPipeline.StreamSummary summary) {
//        log.info("[notify] Bulk " + uploadId + " done: " + summary.totalBytes() + " bytes");
//    }
//}
