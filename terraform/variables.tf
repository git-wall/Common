# Variables definition file
# Define all input variables used in your Terraform configuration

# AWS region for deploying resources
variable "aws_region" {
  # Description of the variable
  description = "The AWS region to deploy resources"
  
  # Type constraint for the variable
  type        = string
  
  # Default value if not specified
  default     = "us-west-2"
}

# Environment name (dev, staging, prod)
variable "environment" {
  description = "Environment name for resource tagging and naming"
  type        = string
  default     = "dev"
}

# Application name
variable "app_name" {
  description = "Name of the application"
  type        = string
  default     = "my-app"
}

# VPC CIDR block
variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

# Public subnet CIDR blocks
variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

# Private subnet CIDR blocks
variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

# Instance type for EC2 instances
variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

# Key pair name for SSH access
variable "key_name" {
  description = "Name of the key pair for SSH access"
  type        = string
  default     = null
}