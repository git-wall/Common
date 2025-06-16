# Terraform Variables File
# Contains the values for variables defined in variables.tf
# Customize these values according to your requirements

# AWS region
aws_region = "us-west-2"

# Environment name
environment = "dev"

# Application name
app_name = "spring-app"

# VPC CIDR block
vpc_cidr = "10.0.0.0/16"

# Public subnet CIDR blocks
public_subnet_cidrs = [
  "10.0.1.0/24",
  "10.0.2.0/24"
]

# Private subnet CIDR blocks
private_subnet_cidrs = [
  "10.0.3.0/24",
  "10.0.4.0/24"
]

# EC2 instance type
instance_type = "t3.micro"

# Key pair name for SSH access
key_name = "my-key-pair"