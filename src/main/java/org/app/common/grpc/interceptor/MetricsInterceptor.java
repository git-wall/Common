package org.app.common.grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interceptor for collecting metrics on gRPC calls.
 */
@Slf4j
@Component
public class MetricsInterceptor implements ServerInterceptor {
    
    private final ConcurrentMap<String, AtomicLong> requestCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> responseTimeAccumulators = new ConcurrentHashMap<>();
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, 
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        String methodName = call.getMethodDescriptor().getFullMethodName();
        long startTime = System.currentTimeMillis();
        
        // Increment request counter
        requestCounters.computeIfAbsent(methodName, k -> new AtomicLong(0L)).incrementAndGet();
        
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                    @Override
                    public void close(Status status, Metadata trailers) {
                        // Calculate response time
                        long responseTime = System.currentTimeMillis() - startTime;
                        responseTimeAccumulators.computeIfAbsent(methodName, k -> new AtomicLong(0L))
                                .addAndGet(responseTime);
                        
                        // Increment error counter if status is not OK
                        if (!status.isOk()) {
                            errorCounters.computeIfAbsent(methodName, k -> new AtomicLong(0L))
                                    .incrementAndGet();
                        }
                        
                        // Log metrics
                        log.debug("gRPC metrics - method: {}, responseTime: {}ms, status: {}", 
                                methodName, responseTime, status.getCode());
                        
                        super.close(status, trailers);
                    }
                }, headers)) {
        };
    }
    
    /**
     * Get the total number of requests for a method.
     * 
     * @param methodName The full method name
     * @return The number of requests
     */
    public long getRequestCount(String methodName) {
        AtomicLong counter = requestCounters.get(methodName);
        return counter != null ? counter.get() : 0L;
    }
    
    /**
     * Get the total number of errors for a method.
     * 
     * @param methodName The full method name
     * @return The number of errors
     */
    public long getErrorCount(String methodName) {
        AtomicLong counter = errorCounters.get(methodName);
        return counter != null ? counter.get() : 0L;
    }
    
    /**
     * Get the average response time for a method.
     * 
     * @param methodName The full method name
     * @return The average response time in milliseconds, or 0 if no requests
     */
    public double getAverageResponseTime(String methodName) {
        AtomicLong timeAccumulator = responseTimeAccumulators.get(methodName);
        AtomicLong requestCounter = requestCounters.get(methodName);
        
        if (timeAccumulator != null && requestCounter != null && requestCounter.get() > 0L) {
            return (double) timeAccumulator.get() / (double) requestCounter.get();
        }
        
        return 0;
    }
}