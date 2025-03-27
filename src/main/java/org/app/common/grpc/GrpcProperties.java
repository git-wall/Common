package org.app.common.grpc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for gRPC.
 */
@Component
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
        private int maxInboundMetadataSize = 8 << 10; //  8 * 1024 8KB
    }
    
    @Getter
    @Setter
    public static class Client {
        private String host = "localhost";
        private int port = 9090;
        private long deadlineMs = 5000L;
        private boolean keepAliveEnabled = true;
        private long keepAliveTimeMs = 30000L; // 30 seconds
        private long keepAliveTimeoutMs = 5000L; // 5 seconds
    }
    
    @Getter
    @Setter
    public static class Auth {
        private boolean enabled = false;
        private String tokenSecret = "";
        private long tokenExpirationMs = 3600000L; // 1 hour
    }
}