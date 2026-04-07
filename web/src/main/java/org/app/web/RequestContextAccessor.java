package org.app.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestContextAccessor {

    private static RequestProvider provider = new DefaultRequestProvider();

    public static void setProvider(RequestProvider newProvider) {
        if (newProvider != null) {
            provider = newProvider;
        }
    }

    public static HttpServletRequest getRequest() {
        return provider.getCurrentRequest();
    }
}
