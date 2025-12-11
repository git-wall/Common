```shell
kubectl get pods -n database
```

```shell
kubectl describe pod -n database postgres-deployment-5c7479744b-dn5lj
```

```shell
kubectl logs -n database <postgres-pod-name>
```
/var/lib/containers/storage/volumes/postgres_postgres_data/_data"

```shell
podman exec -it kind-cluster-control-plane /bin/bash
mkdir -p /tmp/kind/postgres-data
chown 1000:1000 /tmp/kind/postgres-data
chmod 700 /tmp/kind/postgres-data
```

```shell
  kubectl get pv,pvc -n database
```

```shell
  kubectl get pod -n database
```

```shell
kubectl apply -f pv.yml -n database
```

```shell
kubectl apply -f pvc.yml -n database
```

```shell
kubectl apply -f deployment.yml -n database
```
  
```shell
kubectl apply -f statefulset.yml -n database
```

if test kind in podman need create folder for volume mapping
```shell
mkdir -p /path/on/host/postgres_data
```

```shell
  kubectl patch svc ingress-nginx-controller -n ingress-nginx -p "{"spec": {"type": "NodePort"}}"
```

syntax : kubectl port-forward <target> <LOCAL_PORT>:<REMOTE_PORT>
```shell
  kubectl port-forward -n database svc/postgres18-service 5434:5432
```

```shell
kubectl get pods -n database -o wide
```

```shell
  kubectl logs postgres18-pod -n database
```

```shell
  kubectl logs postgres18-0 -n database
```

```shell
  kubectl describe pod postgres18-0 -n database
```

```shell
podman exec -it kind-cluster-control-plane bash
```

```shell
mkdir -p /tmp/kind/postgres-data
chmod 777 /tmp/kind/postgres-data
```

```shell
kubectl delete all --all -n database
```

```shell
  kubectl apply -f k8s/
```

```shell
  kubectl delete statefulset postgres18 -n database
```

```shell
  kubectl delete pod postgres18-0 -n database
```

```shell
  kubectl delete pvc postgres-pvc -n database
```

```shell
  kubectl delete pv postgres-pv -n database
```
```shell
  kubectl delete statefulset postgres18 -n database
```

```shell
  kubectl delete pvc postgres-data-postgres18-0 -n database
```

restart statefulset
```shell
  kubectl rollout restart statefulset/postgres18 -n database 
```

edit file
```shell
sed -i '128s/md5/scram-sha-256/' /data/pgdata/pg_hba.conf
```
