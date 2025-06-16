package org.app.common.client;

public interface ClientInfo {

    String getServiceName();

    /**
     * Get the hostname of the client service
     * @return Hostname string
     */
    String getHostname();

    /**
     * Get the port for the client service
     * @return Port number
     */
    Integer getPort();

    /**
     * Get the schema (http/https) for the client service
     * @return Schema string, defaults to "http"
     */
    default String getSchema() {
        return "http"; // Default schema is http
    }

    /**
     * Get the timeout for client requests
     * @return Timeout in milliseconds, defaults to 5000ms
     */
    default int timeout() {
        return 5000; // Default timeout of 5 seconds
    }

    /**
     * Get the base URL for the client service
     * Combines schema, hostname, and port
     * @return Base URL string
     */
    default String getBaseUrl() {
        return getSchema() + "://" + getHostname() + ":" + getPort();
    }
}
