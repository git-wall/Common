//package org.app.common.design.platform.infrastructure.adapter.payment;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Component("paypal")
//@RequiredArgsConstructor
//@Slf4j
//public class PaypalPaymentAdapter implements PaymentGateway {
//
//    private final PaypalClient paypalClient;
//
//    @Override
//    public PaymentResult processPayment(PaymentRequest request) {
//        log.info("Processing payment via PayPal: {}", request.getAmount());
//
//        var paypalReq = convertToPaypal(request);
//        var paypalResp = paypalClient.createPayment(paypalReq);
//
//        return PaymentResult.builder()
//            .success(paypalResp.getState().equals("approved"))
//            .transactionId(paypalResp.getId())
//            .amount(request.getAmount())
//            .build();
//    }
//
//    private PaypalPaymentRequest convertToPaypal(PaymentRequest request) {
//        return PaypalPaymentRequest.builder()
//            .intent("sale")
//            .amount(request.getAmount().toString())
//            .currency(request.getCurrency())
//            .build();
//    }
//}
