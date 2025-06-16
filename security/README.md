# Security Templates

This directory contains templates for implementing security measures in your Kubernetes and Spring Boot applications.

## Contents

### Network Policies
- `default-deny.yml`: Default deny-all network policy template
- `allow-specific-traffic.yml`: Template for allowing specific traffic between services

### Scanning
- `trivy-config.yml`: Configuration for Trivy vulnerability scanner
- `sonarqube-config.properties`: Configuration for SonarQube code quality and security scanner

### RBAC (Role-Based Access Control)
- `role-templates.yml`: Templates for different Kubernetes roles (developer, devops, admin)
- `service-accounts.yml`: Templates for service accounts and role bindings

### Authentication
- `oauth2-config.yml`: Spring Security OAuth2 configuration
- `keycloak-realm.json`: Keycloak realm configuration for identity management

## Usage

### Network Policies

1. Apply the default deny policy to restrict all traffic:
```bash
    kubectl apply -f network-policies/default-deny.yml
```

2. Customize the allow-specific-traffic policy to allow specific traffic between services:
```bash
    kubectl apply -f network-policies/allow-specific-traffic.yml
```

### Scanning

1. Run Trivy vulnerability scanner:
```bash
    trivy image --config trivy-config.yml your-image:tag
```

2. Run SonarQube analysis:
```bash
    mvn sonar:sonar -Dsonar.host.url=http://your-sonarqube-server
```

### RBAC

1. Apply the role templates:
```bash
    kubectl apply -f rbac/role-templates.yml
    kubectl apply -f rbac/service-accounts.yml
```

### Authentication
Add the OAuth2 configuration to your Spring Boot application's application.yml
Deploy Keycloak with the provided realm configuration
### Best Practices
Always start with a default deny network policy and explicitly allow required traffic
Regularly scan your containers and code for vulnerabilities
Follow the principle of least privilege when assigning roles
Use a centralized identity provider for authentication and authorization
Rotate credentials regularly
Implement proper secrets management
Enable audit logging for security events