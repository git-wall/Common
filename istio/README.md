# Istio Templates for Spring Applications

This directory contains generic Istio templates that can be used with Spring applications.

## Usage

1. Copy the appropriate template for your use case
2. Replace the placeholder variables (enclosed in `${...}`) with your actual values
3. Apply the configuration using `kubectl apply -f your-config.yaml`

## Available Templates

- **virtual-service-template.yaml**: Basic routing configuration
- **destination-rule-template.yaml**: Traffic policies and subsets
- **gateway-template.yaml**: Ingress configuration
- **service-entry-template.yaml**: External service integration
- **circuit-breaker-template.yaml**: Resilience patterns
- **retry-policy-template.yaml**: Automatic retry configuration
- **timeout-policy-template.yaml**: Request timeout settings
- **fault-injection-template.yaml**: Testing failure scenarios
- **traffic-shifting-template.yaml**: Canary/blue-green deployments

## Example Usage

For a Spring Boot service named "customer-service" in the "default" namespace:

```bash
# Replace variables in the virtual service template
cat virtual-service-template.yaml | \
  sed 's/${SERVICE_NAME}/customer-service/g' | \
  sed 's/${NAMESPACE}/default/g' | \
  sed 's/${SERVICE_HOST}/customer-service.example.com/g' | \
  sed 's/${GATEWAY_NAME}/customer-gateway/g' | \
  sed 's/${URI_PREFIX}/api\/customers/g' | \
  sed 's/${DESTINATION_SERVICE}/customer-service/g' | \
  sed 's/${SERVICE_PORT}/8080/g' > customer-virtual-service.yaml

# Apply the configuration
kubectl apply -f customer-virtual-service.yaml