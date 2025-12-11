# Fix PgBouncer restart issue - Generate userlist from PostgreSQL

Write-Host "=== Generating PgBouncer userlist from PostgreSQL ===" -ForegroundColor Green

# Step 1: Get the SCRAM hash from PostgreSQL
Write-Host "`nStep 1: Retrieving password hash from PostgreSQL..." -ForegroundColor Cyan

$getPgHash = @"
kubectl exec -n database -it $(kubectl get pod -n database -l app=postgres18 -o jsonpath='{.items[0].metadata.name}') -- psql -U admin -d postgres -tAc "SELECT passwd FROM pg_shadow WHERE usename='admin';"
"@

Write-Host "Running: $getPgHash" -ForegroundColor Gray
$hash = Invoke-Expression $getPgHash
$hash = $hash.Trim()

Write-Host "Retrieved hash: $hash" -ForegroundColor Green

# Step 2: Create the userlist content
$userlistContent = "`"admin`" `"$hash`""
Write-Host "`nUserlist content:" -ForegroundColor Cyan
Write-Host $userlistContent -ForegroundColor White

# Step 3: Create/Update the userlist secret
Write-Host "`nStep 2: Creating Kubernetes secret with userlist..." -ForegroundColor Cyan

# Delete existing secret if it exists
kubectl delete secret pgbouncer-userlist -n database --ignore-not-found=true

# Create new secret
kubectl create secret generic pgbouncer-userlist `
  --namespace database `
  --from-literal="userlist.txt=$userlistContent"

Write-Host "✓ Secret created: pgbouncer-userlist" -ForegroundColor Green

Write-Host "`n=== Next: Update your Helm deployment ===" -ForegroundColor Yellow
Write-Host "The deployment needs to use the secret instead of emptyDir" -ForegroundColor White
Write-Host "`nAfter updating deployment.yaml, run:" -ForegroundColor Cyan
Write-Host "helm upgrade pgbouncer ./pgbouncer -n database" -ForegroundColor White
