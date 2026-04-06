# PgBouncer Helm Chart

Production-ready Helm chart for deploying PgBouncer connection pooler.

## Quick Start

```powershell
# Validate the chart
helm lint ./pgbouncer

# Install
helm install pgbouncer ./pgbouncer -n database --create-namespace

# Test connection
psql -h localhost -p 5001 -U admin -d postgres
```

## Installation

See full installation guide in INSTALLATION.md

## Configuration

Key configuration options in values.yaml:

- **replicaCount**: Number of PgBouncer replicas
- **auth.username/password**: Database credentials
- **postgresql.host**: Backend PostgreSQL service
- **pgbouncer.poolMode**: transaction, session, or statement
- **autoscaling.enabled**: Enable HPA

## Accessing PgBouncer

From Windows (Kind cluster):
```
psql -h localhost -p 5001 -U admin -d postgres
```

## Monitoring

```powershell
# View pods
kubectl get pods -n database

# View logs
kubectl logs -n database -l app.kubernetes.io/name=pgbouncer -f

# Check HPA
kubectl get hpa -n database
```

## Uninstall

```powershell
helm uninstall pgbouncer -n database
```
