//package org.app.common.design.platform.infrastructure.adapter.payment;
//
//import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.dao.DataAccessException;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Recover;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//
//@Component("stripe")
//@RequiredArgsConstructor
//@Slf4j
//public class StripePaymentAdapter implements PaymentGateway {
//
//    private final StripeClient stripeClient;
//    private final CircuitBreakerRegistry circuitBreakerRegistry;
//
//    @Override
//    @Retryable(
//        value = {DataAccessException.class},
//        maxAttempts = 3,
//        backoff = @Backoff(delay = 1000, multiplier = 2)
//    )
//    public PaymentResult processPayment(PaymentRequest request) {
//
//        log.info("Processing payment via Stripe: {}", request.getAmount());
//
//        var circuitBreaker = circuitBreakerRegistry.circuitBreaker("stripe");
//
//        return circuitBreaker.executeSupplier(() -> {
//            StripeChargeRequest stripeReq = StripeChargeRequest.builder()
//                .amount(convertToCents(request.getAmount()))
//                .currency(request.getCurrency().toLowerCase())
//                .source(request.getToken())
//                .description("Order: " + request.getOrderId())
//                .build();
//
//            StripeChargeResponse stripeResp = stripeClient.charge(stripeReq);
//
//            return PaymentResult.builder()
//                .success(stripeResp.getStatus().equals("succeeded"))
//                .transactionId(stripeResp.getId())
//                .amount(request.getAmount())
//                .message(stripeResp.getOutcome().getMessage())
//                .build();
//        });
//    }
//
//    @Recover
//    public PaymentResult fallback(Exception e, PaymentRequest request) {
//        log.error("Payment failed after retries", e);
//        return PaymentResult.failed("Payment service unavailable");
//    }
//
//    private int convertToCents(BigDecimal amount) {
//        return amount.multiply(new BigDecimal("100")).intValue();
//    }
//}
