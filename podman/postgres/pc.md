
```shell
kubectl apply -f 00-namespace.yaml
kubectl apply -f 01-configmap.yaml -f 02-secret.yaml
kubectl apply -f 03-deployment.yaml
kubectl apply -f 04-service-clusterip.yaml
```

```shell
kubectl get ns,myapp,deploy,svc,pods -n myapp
kubectl describe pod <pod-name> -n myapp
kubectl logs <pod-name> -n myapp -c myapp
kubectl port-forward svc/myapp 8080:80 -n myapp
kubectl exec -it <pod-name> -n myapp -- /bin/sh
kubectl rollout status deployment/myapp-deployment -n myapp
kubectl scale deployment/myapp-deployment --replicas=5 -n myapp
kubectl get hpa -n myapp
```

```shell
kubectl rollout history deployment/myapp-deployment -n myapp
kubectl rollout undo deployment/myapp-deployment -n myapp
```

```shell
kubectl delete -f <file>
# hoặc delete namespace để dọn toàn bộ:
kubectl delete ns myapp
```

Các lỗi phổ biến & cách khắc phục
1. Pods CrashLoopBackOff: kubectl logs xem lỗi, check readiness/liveness khiến restart.
2. Service không route được: kiểm tra selector của Service có khớp pod labels không.
3. Deployment không scale: selector.matchLabels không khớp template.labels.
4. HPA không scale: metrics-server chưa cài; CPU requests chưa set nên HPA không có baseline.
5. Ingress 404: không có Ingress Controller hoặc ingressClass sai.
6. Secret không mount: namespace khác nhau — Secret phải ở cùng namespace với Pod.