### DOS Detect
- use in security filter
```java
@Component
public class DosDetectionFilter implements Filter {

    private final DosDetection dosDetection;

    public DosDetectionFilter(DosDetection dosDetection) {
        this.dosDetection = dosDetection;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String clientIp = request.getRemoteAddr();
        if (dosDetection.isSuspectedDos(clientIp)) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_TOO_MANY_REQUESTS, "Too many requests");
            return;
        }
        chain.doFilter(request, response);
    }
}

```