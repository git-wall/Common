//package org.app.common.security.filter;
//
//import io.github.bucket4j.Bandwidth;
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.Bucket4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class RateLimitFilter extends OncePerRequestFilter {
//
//    private static final Bandwidth limit = Bandwidth.builder()
//        .capacity(100)
//        .refillGreedy(100, Duration.ofHours(1))
//        .build();
//
//    private final Bucket bucket = Bucket4j.builder()
//        .addLimit(limit)
//        .build();
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        var probe = bucket.tryConsumeAndReturnRemaining(1);
//
//        if (probe.isConsumed()) {
//            long remaining = probe.getRemainingTokens();
//            long resetSeconds = probe.getNanosToWaitForRefill() > 0
//                ? TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())
//                : 0;
//
//            response.addHeader(HeaderRateLimit.LIMIT.value, String.valueOf(limit.getCapacity()));
//            response.addHeader(HeaderRateLimit.REMAINING.value, String.valueOf(remaining));
//            response.addHeader(HeaderRateLimit.RESET.value, String.valueOf(Instant.now().getEpochSecond() + resetSeconds));
//
//            filterChain.doFilter(request, response);
//        } else {
//            long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
//            response.addHeader(HttpHeaders.RETRY_AFTER, String.valueOf(waitSeconds));
//
//            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//            response.getWriter().write(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
//        }
//    }
//}
//
//enum HeaderRateLimit {
//    LIMIT("X-RateLimit-Limit"),
//    REMAINING("X-RateLimit-Remaining"),
//    RESET("X-RateLimit-Reset");
//
//    final String value;
//
//    HeaderRateLimit(String value) {
//        this.value = value;
//    }
//}
