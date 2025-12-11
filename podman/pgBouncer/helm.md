```shell
  helm create chart
```

```shell
  helm lint ./pgbouncer-chart
```

```shell
  helm template pgbouncer ./pgbouncer-chart
```

# Validate
```shell
  helm lint .
```

# Install to Kind
```shell
  helm install pgbouncer . -n database --create-namespace
```
list
```shell
helm list -n database
```
neu ton tai can xoa thi xoa roi moi install lai tu dau
```shell
helm uninstall pgbouncer -n database
```

# Test
psql -h localhost -p 5001 -U admin -d postgres

# Dry run to check
```shell
  helm upgrade pgbouncer . -n database --dry-run --debug
```

# Deploy
```shell
  helm upgrade pgbouncer ./pgbouncer -n database
```

# Watch pods
```shell
  kubectl get pods -n database -w
```
```shell
  kubectl logs -n database -l app.kubernetes.io/name=pgbouncer --tail=50 -f
```
```shell
  kubectl describe -n database -l app.kubernetes.io/name=pgbouncer
```
```shell
   kubectl exec -n database -it $(kubectl get pod -n database -l app=postgres18 -o jsonpath='{.items[0].metadata.name}') -- psql -U admin -d postgres -c "SHOW password_encryption;"
```
```shell
  kubectl logs -n database -l app.kubernetes.io/name=pgbouncer --tail=20 -f
```