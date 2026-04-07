Directory Structure

```text
k8s/
├── 01-namespace.yaml
├── 02-configmap.yaml
├── 03-secret.yaml
├── 04-service.yaml
├── 05-deployment.yaml
├── 06-hpa.yaml
├── 07-pvc.yaml
├── 08-ingress.yaml
├── 09-network-policy.yaml
├── 10-rbac.yaml
└── kustomization.yaml
```

Option 1: Apply All Files at Once
```bash
  kubectl apply -f k8s/
```

Option 2: Use Kustomize (Recommended)
```bash
  kubectl apply -k k8s/
```

Kubernetes bao gồm những gì:

* Control Plane: Quản lý toàn bộ cluster với các thành phần như API Server, etcd, Scheduler, Controller Manager
* Nodes: Máy chủ vật lý hoặc máy ảo chạy ứng dụng của bạn
* Pods: Đơn vị nhỏ nhất trong K8s, chứa một hoặc nhiều container
* Services: Cung cấp địa chỉ IP và DNS để truy cập vào các pods
* Deployments: Quản lý việc cập nhật và triển khai các pods
* ConfigMaps và Secrets: Lưu trữ thông tin cấu hình và bí mật
* Ingress: Quản lý truy cập từ bên ngoài vào các services
* Namespaces: Tạo không gian ảo để phân chia tài nguyên trong cluster

with docker-compose can convert like this to k8s
```bash
    kompose convert -f docker-compose.yml
```