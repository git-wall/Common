package org.app.common.component;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Endpoint kiểm tra feature toggle / config runtime <br>
 * - Check feature flag đang ON/OFF <br>
 * - Debug hành vi bất thường trên prod <br>
 * - So sánh giữa các node
 * */
@Component
@Endpoint(id = "feature-flags")
public class FeatureFlagEndpoint {

    @ReadOperation
    public Map<String, Boolean> flags() {
        return Map.of(
            "newCampaignFlow", true,
            "redisCacheV2", false,
            "asyncDistributor", true
        );
    }
}

