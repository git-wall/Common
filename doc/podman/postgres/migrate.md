Dump and restore a PostgreSQL database from a Podman container to a local file and back.
```shell
 docker exec -t postgres18 pg_dumpall -U admin > "E:\Database\dump\postgres_backup.sql"
```

Restore the PostgreSQL database from the local file back into the Podman container.
```shell
 podman exec -i postgres18 psql -U admin -d postgres < "E:\Database\dump\postgres_backup.sql"
```

Podman dump
```shell
 podman exec postgres18 pg_dumpall -U admin > "E:\Database\dump\postgres_backup.sql"
```

kubectl cp <local_path> <namespace>/<pod_name>:<remote_path>
```shell
  cd E:\Database\dump
  kubectl cp postgres_backup.sql database/postgres18-0:/tmp/postgres_backup.sql
  kubectl exec -it postgres18-0 -n database -- psql -U admin -d postgres -f /tmp/postgresbk.sql
```

If you encounter encoding issues during the restore process
convert the SQL file from UTF-16 to UTF-8 before restoring.
```shell
  iconv -f UTF-16 -t UTF-8 /tmp/postgresbk.sql -o /tmp/postgres_backup_utf8.sql
  psql -U admin -d postgres -f /tmp/postgres_backup_utf8.sql
```