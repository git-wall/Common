package org.app.common.auth.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import java.io.IOException;
import java.util.Optional;

@WebFilter(
        servletNames = {"intercepting-filter"},
        initParams = {@WebInitParam(name = "encoding", value = "UTF-8")}
)
public class EncodingFilter extends BaseFilter {
    private String encoding;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.encoding = filterConfig.getInitParameter("encoding");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        String e = Optional
                .ofNullable(request.getParameter("encoding"))
                .orElse(this.encoding);
        response.setCharacterEncoding(e);
        chain.doFilter(request, response);
    }
}
