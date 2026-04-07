```env
env:
  - name: POD_NAME
    valueFrom:
      fieldRef:
        fieldPath: metadata.name
  - name: POD_IP
    valueFrom:
      fieldRef:
        fieldPath: status.podIP
  - name: NODE_NAME
    valueFrom:
      fieldRef:
        fieldPath: spec.nodeName
```

```properties
management.endpoints.web.base-path=/actuator
management.endpoints.web.exposure.include=health,info,metrics,identity,build-info,runtime-info,cache-status,thread-summary,feature-flags

management.endpoint.health.show-details=never
management.endpoint.health.probes.enabled=true
```