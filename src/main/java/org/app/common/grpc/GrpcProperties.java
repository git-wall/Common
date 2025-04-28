package org.app.common.grpc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for gRPC.
 */
@ConfigurationProperties(prefix = "grpc")
@Getter
@Setter
public class GrpcProperties {
    
    private Server server = new Server();
    private Client client = new Client();
    private Auth auth = new Auth();
    
    @Getter
    @Setter
    public static class Server {
        private int port = 9090;
        private boolean enabled = true;
        private int maxInboundMessageSize = 4 * 1024 * 1024; // 4MB
        private int maxInboundMetadataSize = 8 << 10; // 8KB
        private int maxConcurrentCalls = 16;
        private String certChainFile = "";
        private String privateKeyFile = "";
    }
    
    @Getter
    @Setter
    public static class Client {
        private String host = "localhost";
        private int port = 9090;
        private boolean enabled = true;
        private long deadlineMs = 5000L;
        private boolean keepAliveEnabled = true;
        private long keepAliveTimeMs = 30000L; // 30 seconds
        private long keepAliveTimeoutMs = 5000L; // 5 seconds
        private int maxInboundMessageSize = 4 * 1024 * 1024; // 4MB
        private boolean usePlaintext = true;
        private String trustCertCollectionFile = "";
        private int maxRetryAttempts = 3;
        private long retryDelayMs = 1000L;
    }
    
    @Getter
    @Setter
    public static class Auth {
        private boolean enabled = false;
        private String tokenSecret = "";
        private long tokenExpirationMs = 3600000L; // 1 hour
        private String issuer = "grpc-service";
        private String audience = "grpc-client";
    }
}