<<<<<<<< HEAD:security/src/main/java/org/app/security/security/opa/OpaClient.java
package org.app.security.security.opa;
========
package org.app.security.opa;
>>>>>>>> dev:security/src/main/java/org/app/security/opa/OpaClient.java

import org.app.common.client.http.WebClient;
import org.springframework.http.HttpMethod;

public class OpaClient {
    private final WebClient webClient;
    private final OpaProperties opaProperties;
    public OpaClient(WebClient webClient, OpaProperties opaProperties) {
        this.webClient = webClient;
        this.opaProperties = opaProperties;
    }

    public boolean isAllowed(Object body) {
        try {
            OpaResponse response = webClient.read(HttpMethod.POST, body, opaProperties.getUri(), OpaResponse.class);
            return response != null && response.result.allow;
        } catch (Exception e) {
            return false;
        }
    }

    public static class OpaResponse {
        Result result;
    }

    public static class Result {
        boolean allow;
    }
}
