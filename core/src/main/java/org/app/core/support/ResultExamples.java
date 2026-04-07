//package org.app.core.support;
//
//import lombok.Data;
//import org.app.core.exception.business.NotFoundException;
//
//// ══════════════════════════════════════════════════════════════════════════════
//// Ví dụ kết hợp Result<T> + Verify
//// ══════════════════════════════════════════════════════════════════════════════
//public class ResultExamples {
//
//    @Data
//    public static class User {
//        String email;
//        boolean isBanned;
//    }
//
//    // ── 1. Basic: lấy data + validate bằng Verify ────────────────
//    void example1() {
//        Result<User> result = Result.of(() -> userRepo.findById(42))
//            .validate(u -> Verify.ifNull(u.getEmail(), "Email required"))
//            .validate(u -> Verify.ifTrue(u.isBanned(), "User is banned"))
//            .validate(u -> Verify.requireBetween(u.getAge(), 0, 120, "Age invalid"));
//
//        result
//            .onSuccess(u -> System.out.println("OK: " + u.getName()))
//            .onFailure(e -> System.out.println("Fail: " + e.getMessage()));
//    }
//
//    // ── 2. Chain nhiều bước, validate từng bước ───────────────────
//    void example2() {
//        Result.of(() -> userRepo.findById(42))
//            .validate(u -> Verify.ifNull(u, "User not found"))
//            .flatMap(u -> Result.of(() -> orderRepo.findLatest(u)))
//            .validate(o -> Verify.ifEmpty(o.getItems(), "Order has no items"))
//            .flatMap(o -> Result.of(() -> emailService.send(o)))
//            .onSuccess(msg -> log.info("Sent: {}", msg))
//            .onFailure(e -> log.error("Error: {}", e.getMessage()));
//    }
//
//    // ── 3. Từ Optional ────────────────────────────────────────────
//    void example3() {
//        Result<User> result = Result.of(
//            userRepo.findById(42),          // trả về Optional<User>
//            "User not found"
//        );
//    }
//
//    // ── 4. validateIf — validate có điều kiện ─────────────────────
//    void example4() {
//        Result.of(() -> userRepo.findById(42))
//            .validateIf(
//                u -> u.getRole() == Role.ADMIN,
//                u -> Verify.ifEmpty(u.getPermissions(), "Admin must have permissions")
//            )
//            .onSuccess(u -> System.out.println("Ready"));
//    }
//
//    // ── 5. mapIf / flatMapIf — transform có điều kiện ────────────
//    void example5() {
//        Result.of(() -> orderRepo.findById(99))
//            .mapIf(
//                o -> o.getStatus() == Status.PENDING,
//                o -> o.withStatus(Status.CONFIRMED)   // chỉ update nếu đang pending
//            )
//            .flatMapIf(
//                o -> o.needsShipping(),
//                o -> Result.of(() -> shippingService.dispatch(o))
//            )
//            .onSuccess(o -> System.out.println("Done"))
//            .onFailure(e -> System.out.println("Fail: " + e.getMessage()));
//    }
//
//    // ── 6. recover — fallback khi lỗi ────────────────────────────
//    void example6() {
//        User user = Result.of(() -> userRepo.findById(42))
//            .recover(err -> Result.of(() -> cacheRepo.findById(42)))   // thử cache
//            .recoverIf(                                                 // chỉ recover nếu đúng type lỗi
//                NotFoundException.class,
//                err -> Result.ok(User.guest())
//            )
//            .getOrElse(User::guest);
//    }
//
//    // ── 7. peek — log giữa chain, không làm gián đoạn ────────────
//    void example7() {
//        Result.of(() -> userRepo.findById(42))
//            .peek(u -> log.debug("Fetched user: {}", u.getId()))
//            .flatMap(u -> Result.of(() -> orderRepo.findLatest(u)))
//            .peek(o -> log.debug("Fetched order: {}", o.getId()))
//            .throwIfFail();
//    }
//
//    // ── 8. throwIfFail — propagate ra ngoài cuối chain ───────────
//    void example8() {
//        User user = Result.of(() -> userRepo.findById(42))
//            .validate(u -> Verify.ifNull(u.getEmail(), "Email required"))
//            .throwIfFail()   // throw VerifyException nếu có lỗi
//            .get();
//    }
//}
