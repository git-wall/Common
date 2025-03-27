### Use this
```java
// RequestIdInterceptor use for init request id in thread context
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(SpringContext
            .getContext()
            .getBean(RequestIdInterceptor.class));
    WebMvcConfigurer.super.addInterceptors(registry);
}
```

**LogRequestInterceptor, AuthRequestInterceptor 
use for config Res-template or WebClient call api inside logic handler**
