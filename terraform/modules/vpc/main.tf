# VPC Module - Main Configuration
# This module creates a VPC with public and private subnets

# Create a VPC
resource "aws_vpc" "main" {
  # CIDR block for the VPC
  cidr_block = var.vpc_cidr
  
  # Enable DNS hostnames for the VPC
  enable_dns_hostnames = true
  
  # Enable DNS support for the VPC
  enable_dns_support   = true
  
  # Tags for the VPC resource
  tags = {
    Name        = "${var.app_name}-${var.environment}-vpc"
    Environment = var.environment
  }
}

# Create an Internet Gateway
resource "aws_internet_gateway" "main" {
  # VPC ID to attach the Internet Gateway to
  vpc_id = aws_vpc.main.id
  
  # Tags for the Internet Gateway
  tags = {
    Name        = "${var.app_name}-${var.environment}-igw"
    Environment = var.environment
  }
}

# Create public subnets
resource "aws_subnet" "public" {
  # Count based on the number of CIDR blocks provided
  count = length(var.public_subnet_cidrs)
  
  # VPC ID for the subnet
  vpc_id = aws_vpc.main.id
  
  # CIDR block for the subnet
  cidr_block = var.public_subnet_cidrs[count.index]
  
  # Availability Zone for the subnet
  availability_zone = var.availability_zones[count.index % length(var.availability_zones)]
  
  # Map public IP on launch
  map_public_ip_on_launch = true
  
  # Tags for the subnet
  tags = {
    Name        = "${var.app_name}-${var.environment}-public-subnet-${count.index + 1}"
    Environment = var.environment
    Type        = "Public"
  }
}

# Create private subnets
resource "aws_subnet" "private" {
  # Count based on the number of CIDR blocks provided
  count = length(var.private_subnet_cidrs)
  
  # VPC ID for the subnet
  vpc_id = aws_vpc.main.id
  
  # CIDR block for the subnet
  cidr_block = var.private_subnet_cidrs[count.index]
  
  # Availability Zone for the subnet
  availability_zone = var.availability_zones[count.index % length(var.availability_zones)]
  
  # Tags for the subnet
  tags = {
    Name        = "${var.app_name}-${var.environment}-private-subnet-${count.index + 1}"
    Environment = var.environment
    Type        = "Private"
  }
}

# Create a route table for public subnets
resource "aws_route_table" "public" {
  # VPC ID for the route table
  vpc_id = aws_vpc.main.id
  
  # Tags for the route table
  tags = {
    Name        = "${var.app_name}-${var.environment}-public-rt"
    Environment = var.environment
  }
}

# Create a route to the Internet Gateway
resource "aws_route" "public_internet_gateway" {
  # Route table ID
  route_table_id         = aws_route_table.public.id
  
  # Destination CIDR block (all traffic)
  destination_cidr_block = "0.0.0.0/0"
  
  # Internet Gateway ID
  gateway_id             = aws_internet_gateway.main.id
}

# Associate public subnets with the public route table
resource "aws_route_table_association" "public" {
  # Count based on the number of public subnets
  count = length(aws_subnet.public)
  
  # Subnet ID
  subnet_id      = aws_subnet.public[count.index].id
  
  # Route table ID
  route_table_id = aws_route_table.public.id
}