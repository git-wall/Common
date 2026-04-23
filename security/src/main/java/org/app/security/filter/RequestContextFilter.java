package org.app.security.filter;

import org.app.network.Network;
import org.app.observation.context.ContextHolder;
import org.app.observation.context.ContextKey;
import org.app.observation.context.RequestContext;
import org.app.web.RequestUtils;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class RequestContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String host = RequestUtils.getHost(httpRequest);

        RequestContext ctx = RequestContext.builder()
            .requestId(MDC         .get(ContextKey.REQUEST_ID))
            .method   (httpRequest .getMethod())
            .path     (httpRequest .getRequestURI())
            .clientIp (RequestUtils.getRemoteAddress(httpRequest))
            .serviceIp(Network     .localHostAddress())
            .domain   (RequestUtils.getDomain(host))
            .userAgent(RequestUtils.getUserAgent(httpRequest))
            .deviceId (RequestUtils.getDeviceId(httpRequest))
            .curl     (RequestUtils.getCurl(httpRequest))
            .host     (host)
            .time     (System.currentTimeMillis())
            .build();

        ContextHolder.setRequest(ctx);
        try {
            chain.doFilter(request, response);
        } finally {
            ContextHolder.clearRequest();
        }
    }
}

