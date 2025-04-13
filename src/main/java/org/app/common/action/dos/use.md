### DOS Detect
- use in security filter
```java
@Component
public class DosDetectionFilter implements Filter {

    private final DosDetectionService dosDetectionService;

    public DosDetectionFilter(DosDetectionService dosDetectionService) {
        this.dosDetectionService = dosDetectionService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String clientIp = request.getRemoteAddr();
        if (dosDetectionService.isSuspectedDos(clientIp)) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_TOO_MANY_REQUESTS, "Too many requests");
            return;
        }
        chain.doFilter(request, response);
    }
}

```