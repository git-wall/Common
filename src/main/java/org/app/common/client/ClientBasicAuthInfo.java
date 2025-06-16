package org.app.common.client;

/**
 * Interface for providing client information for internal API calls
 */
public interface ClientBasicAuthInfo extends ClientInfo {
    /**
     * Get the username for basic authentication
     *
     * @return Username string
     */
    String getUsername();

    /**
     * Get the password for basic authentication
     *
     * @return Password string
     */
    String getPassword();

    /**
     * Determine if request body should be buffered
     *
     * @return true if request body should be buffered, false for streaming
     */
    boolean isBufferRequestBody();
}
