# EC2 Module - Variables
# Define all input variables used in the EC2 module

# VPC ID
variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

# Application name
variable "app_name" {
  description = "Name of the application"
  type        = string
}

# Environment name
variable "environment" {
  description = "Environment name for resource tagging"
  type        = string
}

# Subnet IDs for EC2 instances
variable "subnet_ids" {
  description = "List of subnet IDs for EC2 instances"
  type        = list(string)
}

# Instance type
variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

# Number of instances to create
variable "instance_count" {
  description = "Number of EC2 instances to create"
  type        = number
  default     = 1
}

# Key pair name for SSH access
variable "key_name" {
  description = "Name of the key pair for SSH access"
  type        = string
  default     = null
}

# User data script
variable "user_data" {
  description = "User data script for instance initialization"
  type        = string
  default     = null
}