# Velero Kubernetes Backup and Restore Cheat Sheet

## Overview
Velero is an open-source tool to safely backup and restore, perform disaster recovery, and migrate Kubernetes cluster resources and persistent volumes. This cheat sheet provides essential commands and best practices for using Velero in your Kubernetes environment.

## Installation
### Prerequisites
- Kubernetes cluster
- kubectl CLI configured
- Storage provider credentials (AWS, GCP, Azure, etc.)
### Install Velero CLI

```bash
    # Download Velero
    wget https://github.com/vmware-tanzu/velero/releases/download/v1.11.0/velero-v1.11.0-windows-amd64.tar.gz
    
    # Extract the archive
    tar -xvf velero-v1.11.0-windows-amd64.tar.gz
    
    # Move the binary to your PATH
    move velero-v1.11.0-windows-amd64\velero.exe C:\path\to\your\bin\directory
```

### Install Velero Server on Kubernetes

```bash
    # Using AWS S3
    velero install \
      --provider aws \
      --plugins velero/velero-plugin-for-aws:v1.6.0 \
      --bucket velero-backups \
      --backup-location-config region=us-east-1 \
      --secret-file ./credentials-velero
    
    # Using Azure
    velero install \
      --provider azure \
      --plugins velero/velero-plugin-for-microsoft-azure:v1.6.0 \
      --bucket velero-backups \
      --backup-location-config resourceGroup=velero,storageAccount=velerobackups \
      --secret-file ./credentials-velero
    
    # Using GCP
    velero install \
      --provider gcp \
      --plugins velero/velero-plugin-for-gcp:v1.6.0 \
      --bucket velero-backups \
      --secret-file ./credentials-velero
```

## Basic Operations
### Backup

```bash
    # Backup entire cluster
    velero backup create full-cluster-backup
    
    # Backup specific namespace
    velero backup create namespace-backup --include-namespaces=my-namespace
    
    # Backup with specific labels
    velero backup create app-backup --selector app=myapp
    
    # Backup with TTL (Time-To-Live)
    velero backup create backup-with-ttl --ttl 24h
    
    # Backup with hooks (pre/post)
    velero backup create backup-with-hooks --hooks-enabled
    
    # check 
    velero backup get
```

### Restore

```bash
    # List available backups
    velero backup get
    
    # Restore entire backup
    velero restore create --from-backup full-cluster-backup
    
    # Restore to different namespace
    velero restore create --from-backup namespace-backup --namespace-mappings source-ns:target-ns
    
    # Restore specific resources
    velero restore create --from-backup app-backup --include-resources deployments,services
    
    # Restore with resource filtering
    velero restore create --from-backup full-cluster-backup --selector app=myapp
    
    # check
    velero restore get
```

### Schedule Backups

```bash
    # Create a backup schedule (every 24 hours)
    velero schedule create daily-backup --schedule="0 0 * * *"
    
    # Schedule with specific namespace
    velero schedule create hourly-app-backup --schedule="0 * * * *" --include-namespaces=app-namespace
    
    # List schedules
    velero schedule get
    
    # Delete a schedule
    velero schedule delete daily-backup
    
    # by default velero will keep backup 30day you can set --ttl
    # if you want combo skill backup from k8s (PVC,Deployment,Service,ConfigMap,Secret)
    # here you are
    velero schedule create full-nightly-backup \
      --schedule="0 2 * * *" \
      --include-namespaces=default,mysql,app \
      --include-resources=persistentvolumeclaims,deployments,services,configmaps,secrets \
      --ttl=72h
      
     # if you doing with GitOps here is yaml for manager
    apiVersion: velero.io/v1
    kind: Schedule
    metadata:
      name: mysql-daily-backup
      namespace: velero
    spec:
      schedule: "0 2 * * *"
      template:
        metadata:
          labels:
            app: mysql
        spec:
          includedNamespaces:
            - mysql
          storageLocation: default
          ttl: 72h
```

## Advanced Operations
### Managing Backups

```bash
    # Describe backup details
    velero backup describe my-backup
    
    # View backup logs
    velero backup logs my-backup
    
    # Delete a backup
    velero backup delete my-backup
    
    # Download backup contents
    velero backup download my-backup
```

### Managing Restores

```bash
    # Describe restore details
    velero restore describe my-restore
    
    # View restore logs
    velero restore logs my-restore
    
    # Delete a restore
    velero restore delete my-restore
```

### Persistent Volume Backup

```bash
    # Backup with PVs
    velero backup create backup-with-pv --include-namespaces=app-namespace --snapshot-volumes
    
    # Backup with PVs but skip some PVCs
    velero backup create selective-pv-backup --include-namespaces=app-namespace --snapshot-volumes --exclude-resources persistentvolumeclaims
```

## Troubleshooting
### Common Issues

```bash
    # Check Velero server logs
    kubectl logs deployment/velero -n velero
    
    # Check Velero CRDs
    kubectl get backups -n velero
    kubectl get restores -n velero
    kubectl get schedules -n velero
    
    # Check backup storage location status
    velero backup-location get
```

## Debugging Tips
- Ensure your storage provider credentials are correct 
- Verify network connectivity to your storage bucket 
- Check for resource quotas or limits in your cluster 
- Examine Velero server pod status and events
## Best Practices
1. Regular Testing: Periodically test your backup and restore process
2. Versioning: Use meaningful names and labels for your backups
3. Retention Policy: Set appropriate TTL for backups to manage storage costs
4. Exclude Unnecessary Resources: Use --exclude-resources to skip temporary resources
5. Secure Credentials: Properly manage and rotate storage provider credentials
6. Monitor Backup Status: Set up alerts for failed backups
7. Documentation: Keep track of backup procedures and recovery plans
