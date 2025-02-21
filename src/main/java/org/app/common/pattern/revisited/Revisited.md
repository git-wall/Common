Some pattern is convert to revisited

Fluent + chain of responsibility = Java Stream, Optional (JDK 9+) 
Service locator = ApplicationContext in Spring
Builder = lombok
CircuitBreaker = Spring cloud
RateLimiter = Spring cloud
Service registry = Spring cloud Eureka
Load balancer = Spring cloud gateway

///////////////////
Monad = (io.vavr:vavr:1.0.0-alpha-4)
Example:
Try.of(() -> roleRepository.findByName(dto.getRole()))
.filter(Optional::isPresent, () -> new IllegalArgumentException("Not found role with " + dto.getRole()))
.andThen(accountRepository::save)
.map(account -> UserInfo.builder()
.andThen(userInfoRepository::save)
.get();
///////////////////

