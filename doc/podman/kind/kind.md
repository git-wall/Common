```shell
   podman play kube kind.yml
```

view config file
```shell
kubectl config view --minify
```

change port
```shell
kubectl config set-cluster kind-kind-cluster --server=https://127.0.0.1:6443
```

```shell
sudo mkdir -p /mnt/postgres_data/postgres
sudo chown -R 999:999 /mnt/postgres_data/postgres
sudo chmod -R 700 /mnt/postgres_data/postgres
```