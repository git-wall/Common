# Couchbase
- Document-based NoSQL database
- High performance and scalability
- Built-in caching capabilities
- Support for JSON documents

`Attention -> Couchbase has RBAC (Role-Based Access Control) and encryption be carefully for config`

### **Strong**
- good with refresh token
- invoke token
- blacklist
- user profiles with roles, permissions

### **Weakness**
- access token with TTL -> with this case use Redis

### Config and use
```yaml
couchbase:
  enabled: true
  connection-string: couchbase://localhost
  username: administrator
  password: 123
  bucket-name: default
```

