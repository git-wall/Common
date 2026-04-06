podman run -d \
  --name postgres-container \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=postgres \
  -p 5434:5432 \
  -v "D:\Database\postgres_data:/var/lib/postgresql/data:Z" \
  postgres:18
