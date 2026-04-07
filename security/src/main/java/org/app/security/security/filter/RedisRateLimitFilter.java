//package org.app.common.security.filter;
//
//import io.github.bucket4j.Bandwidth;
//import io.github.bucket4j.BucketConfiguration;
//import io.github.bucket4j.distributed.BucketProxy;
//import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
//import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
//import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
//import io.lettuce.core.RedisClient;
//import io.lettuce.core.api.StatefulRedisConnection;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
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
//public class RedisRateLimitFilter extends OncePerRequestFilter {
//
//    @Value("${redis.uri:redis://localhost:6379}")
//    private String redisUri;
//    private RedisClient redisClient;
//    private StatefulRedisConnection<String, String> connection;
//    private LettuceBasedProxyManager<byte[]> proxyManager;
//
//    private final Bandwidth limit = Bandwidth.builder()
//        .capacity(100)
//        .refillGreedy(100, Duration.ofHours(1))
//        .build();
//
//    private final BucketConfiguration bucketConfiguration = BucketConfiguration.builder()
//        .addLimit(limit)
//        .build();
//
//    @PostConstruct
//    public void init() {
//        redisClient = RedisClient.create(redisUri);
//        connection = redisClient.connect();
//
//        proxyManager = Bucket4jLettuce.casBasedBuilder((RedisClient) connection)
//            .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
//            .build();
//    }
//
//    @PreDestroy
//    public void cleanup() {
//        if (connection != null) {
//            connection.close();
//        }
//        if (redisClient != null) {
//            redisClient.shutdown();
//        }
//    }
//
//    private BucketProxy resolveBucket(String clientKey) {
//        return proxyManager.getProxy(clientKey.getBytes(), () -> bucketConfiguration);
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String clientKey = resolveClientKey(request);
//        BucketProxy bucket = resolveBucket(clientKey);
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
//            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//            response.addHeader(HttpHeaders.RETRY_AFTER, String.valueOf(waitSeconds));
//            response.getWriter().write(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
//        }
//    }
//
//    private String resolveClientKey(HttpServletRequest request) {
//        return "rate_limit:" + request.getRemoteAddr();
//    }
//}
