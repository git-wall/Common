```yml
# application.yml
server:
  port: 8080
  tomcat:
    threads:
      max: 200                    # Max threads
      min-spare: 10
    max-connections: 10000        # Max concurrent connections
    accept-count: 100             # Backlog queue
    connection-timeout: 20000     # 20s
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
          fetch_size: 50
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
            
  task:
    execution:
      pool:
        core-size: 10
        max-size: 50
        queue-capacity: 100
```