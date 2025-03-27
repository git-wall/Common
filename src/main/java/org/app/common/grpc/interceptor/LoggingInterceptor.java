package org.app.common.grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Interceptor for logging gRPC requests and responses.
 */
@Slf4j
@Component
public class LoggingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.info("gRPC call: {}", methodName);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                    @Override
                    public void sendMessage(RespT message) {
                        log.debug("gRPC response for {}: {}", methodName, message);
                        super.sendMessage(message);
                    }

                    @Override
                    public void close(Status status, Metadata trailers) {
                        log.info("gRPC call completed: {} with status: {}", methodName, status);
                        super.close(status, trailers);
                    }
                }, headers)) {

            @Override
            public void onMessage(ReqT message) {
                log.debug("gRPC request for {}: {}", methodName, message);
                super.onMessage(message);
            }
        };
    }
}