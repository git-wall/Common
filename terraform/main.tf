# Main Terraform configuration file
# This file serves as the entry point for your Terraform project

# Specify the required Terraform version
terraform {
  # Minimum required Terraform version
  required_version = ">= 1.0.0"
  
  # Define required providers with their source and version constraints
  required_providers {
    # AWS provider configuration
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
  
  # Backend configuration for storing Terraform state
  # Uncomment and configure as needed
  # backend "s3" {
  #   bucket = "terraform-state-bucket"
  #   key    = "path/to/state/file"
  #   region = "us-west-2"
  # }
}

# Configure the AWS Provider with region
# Replace the region value with your desired AWS region
provider "aws" {
  region = var.aws_region
}

# Include modules as needed
# module "vpc" {
#   source = "./modules/vpc"
#   # Pass variables to the module
#   vpc_cidr = var.vpc_cidr
# }

# Output important information
output "region" {
  description = "AWS region used for resources"
  value       = var.aws_region
}