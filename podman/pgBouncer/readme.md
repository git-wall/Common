## configmap.yaml
### PRODUCTION AUTH
Nên setup loại bảo mật
User có quyền thực thi cao nhất default postgres theo khuyến nghị của nhóm pgbouner
File đường dẫn các user có quyền truy cập
Query để lấy thông tin user có thể được tạo ra để apply vào file
```yaml
    auth_type = {{ .Values.pgbouncer.authType }}
    auth_user = {{ .Values.pgbouncer.authUser }}
    auth_file = {{ .Values.pgbouncer.authFile }}
    auth_query = {{ .Values.pgbouncer.authQuery }}
```

### Mục database
có thể thêm hàng tức là thêm pool cho nhóm service nào đó 
```yaml
    [databases]
    * = host={{ .Values.postgresql.host }} port={{ .Values.postgresql.port }}
```
setup cho từng pool
```yaml
    ; Connection limits
    max_client_conn = {{ .Values.pgbouncer.maxClientConn }}
    default_pool_size = {{ .Values.pgbouncer.defaultPoolSize }}
    min_pool_size = {{ .Values.pgbouncer.minPoolSize }}
    reserve_pool_size = {{ .Values.pgbouncer.reservePoolSize }}
    max_db_connections = {{ .Values.pgbouncer.maxDbConnections }}
    max_user_connections = {{ .Values.pgbouncer.maxUserConnections }}
```