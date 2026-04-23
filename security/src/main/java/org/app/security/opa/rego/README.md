## Best Practices
1. Default Deny : Always start with a default deny rule and explicitly allow access
2. Separation of Concerns : Keep policies modular and focused on specific aspects
3. Policy Testing : Write comprehensive tests for your policies
4. Caching : Implement caching for policy decisions to improve performance
5. Monitoring : Log policy decisions for auditing and debugging
6. Versioning : Version your policies and test changes before deployment
7. Documentation : Document the intent and behavior of your policies

### CMD

```bash
# Run OPA server
opa run --server

# Evaluate a policy
opa eval -i input.json -d policy.rego "data.example.allow"

# Test policies
opa test policy.rego policy_test.rego

# Format Rego files
opa fmt -w policy.rego

# Check for policy errors
opa check policy.rego
```