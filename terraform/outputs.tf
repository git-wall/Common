# Outputs definition file
# Define all outputs from your Terraform configuration

# AWS region
output "aws_region" {
  description = "The AWS region used for resources"
  value       = var.aws_region
}

# VPC ID
output "vpc_id" {
  description = "The ID of the VPC"
  value       = module.vpc.vpc_id
}

# VPC CIDR block
output "vpc_cidr_block" {
  description = "The CIDR block of the VPC"
  value       = module.vpc.vpc_cidr_block
}

# Public subnet IDs
output "public_subnet_ids" {
  description = "List of public subnet IDs"
  value       = module.vpc.public_subnet_ids
}

# Private subnet IDs
output "private_subnet_ids" {
  description = "List of private subnet IDs"
  value       = module.vpc.private_subnet_ids
}

# EC2 instance IDs
output "instance_ids" {
  description = "List of EC2 instance IDs"
  value       = module.ec2.instance_ids
}

# EC2 instance public IPs
output "instance_public_ips" {
  description = "List of public IP addresses of EC2 instances"
  value       = module.ec2.instance_public_ips
}