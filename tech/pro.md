| Tool/Technology    | Description                   |
|--------------------|-------------------------------|
| Helm               | Manages application packages  |
| ArgoCD             | GitOps deployment             |
| Prometheus/Grafana | Monitoring                    |
| Loki/Promtail      | Log management                |
| Velero             | Backup & restore cluster      |
| OPA                | Security policies             |

# Velero Architecture

```plaintext
┌─────────────────────┐     ┌─────────────────────┐
│   Kubernetes API    │     │   Storage Provider  │
│                     │     │  (S3, Azure, GCP)   │
└─────────────────────┘     └─────────────────────┘
          ▲                           ▲
          │                           │
          │                           │
┌─────────────────────┐     ┌─────────────────────┐
│   Velero Server     │     │   Velero Plugins    │
│   (Deployment)      │────▶│   (Storage, PV)     │
└─────────────────────┘     └─────────────────────┘
          ▲
          │
          │
┌─────────────────────┐
│   Velero Client     │
│   (CLI)             │
└─────────────────────┘
```

# Event backbone

```plaintext
[Producer] ---> [Consumer] ---> [Service-2]
                    |
              -------------
              Flink   Spark   
```

{link: https://miro.medium.com/v2/resize:fit:1100/format:webp/1*wWDPsOTm1Du7H75kOGPvlQ.png}