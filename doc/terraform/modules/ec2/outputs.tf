# EC2 Module - Outputs
# Define all outputs from the EC2 module

# Instance IDs
output "instance_ids" {
  description = "List of instance IDs"
  value       = aws_instance.app[*].id
}

# Instance public IPs
output "instance_public_ips" {
  description = "List of public IP addresses of instances"
  value       = aws_instance.app[*].public_ip
}

# Instance private IPs
output "instance_private_ips" {
  description = "List of private IP addresses of instances"
  value       = aws_instance.app[*].private_ip
}

# Security group ID
output "security_group_id" {
  description = "ID of the security group"
  value       = aws_security_group.instance.id
}