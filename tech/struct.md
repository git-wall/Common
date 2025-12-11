# DevOps Stack Overview

| Vai trò | Tool khuyên dùng | Giải thích |
|----------|------------------|------------|
| **Source code** | GitHub / GitLab | Code lưu ở đây |
| **CI/CD** | GitHub Actions / GitLab CI / Jenkins | Build, test, build image, push lên registry |
| **Container Registry** | Harbor / GitHub Container Registry / AWS ECR | Nơi lưu image Docker |
| **Deploy** | Helm + ArgoCD | Triển khai tự động lên Kubernetes cluster |
| **Cluster runtime** | Kubernetes (K3s / RKE2 / EKS / GKE) | Orchestrate, autoscale, networking |
| **Network CNI** | Calico / Cilium | Giao tiếp giữa pod và node |
| **Ingress** | NGINX Ingress / Traefik | Nhận traffic từ outside |
| **Secret & config** | Vault / K8s Secret / External Secrets | Lưu mật khẩu, token, config |
| **Monitoring** | Prometheus + Grafana | Quan sát metric |
| **Logging** | Loki / ELK Stack | Thu log |
| **Security** | Falco / Trivy / NetworkPolicy | Scan image, giám sát runtime |
| **Infra provision** | Terraform + Ansible | Tạo VM, cấu hình cluster |
| **Storage** | Longhorn / Ceph / EBS | Volume lưu dữ liệu cho DB |
