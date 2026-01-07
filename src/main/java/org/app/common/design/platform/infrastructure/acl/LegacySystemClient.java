package org.app.common.design.platform.infrastructure.acl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(LegacySystemProperties.class)
public class LegacySystemClient {

    private final RestTemplate restTemplate;
    private final LegacySystemProperties properties;

    /**
     * Fetch order từ legacy system via HTTP
     */
    public LegacyOrder getOrder(String legacyOrderId) {

        String url = properties.getBaseUrl() + "/api/orders/" + legacyOrderId;

        log.info("Fetching order from legacy system: {}", url);

        try {
            LegacyOrderResponse response = restTemplate.getForObject(url, LegacyOrderResponse.class);

            if (response == null || !response.isSuccess()) {
                throw new RuntimeException(
                    "Failed to fetch order: " + (response != null ? response.getMessage() : "null response")
                );
            }

            return response.getData();
        } catch (HttpClientErrorException e) {
            log.error("HTTP error calling legacy system: {}", e.getStatusCode());
            throw new RuntimeException("Legacy system returned error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Network error calling legacy system", e);
            throw new RuntimeException("Cannot connect to legacy system", e);
        } catch (Exception e) {
            log.error("Unexpected error calling legacy system", e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Update order trong legacy system
     */
    public void updateOrder(LegacyOrder legacyOrder) {

        String url = properties.getBaseUrl() + "/api/orders/" + legacyOrder.getOrderNum();

        log.info("Updating order in legacy system: {}", legacyOrder.getOrderNum());

        try {
            // Call HTTP PUT
            restTemplate.put(url, legacyOrder);

            log.info("Successfully updated order in legacy system");

        } catch (Exception e) {
            log.error("Failed to update order in legacy system", e);
            throw new RuntimeException("Cannot update order", e);
        }
    }

    /**
     * Create order trong legacy system
     */
    public String createOrder(LegacyOrder legacyOrder) {

        String url = properties.getBaseUrl() + "/api/orders";

        log.info("Creating order in legacy system");

        try {
            LegacyOrderResponse response = restTemplate.postForObject(
                url,
                legacyOrder,
                LegacyOrderResponse.class
            );

            if (response == null || !response.isSuccess()) {
                throw new RuntimeException("Failed to create order in legacy");
            }

            return response.getData().getOrderNum();

        } catch (Exception e) {
            log.error("Failed to create order in legacy system", e);
            throw new RuntimeException("Cannot create order", e);
        }
    }
}
