# EC2 Module - Main Configuration
# This module creates EC2 instances in the specified subnets

# Get the latest Amazon Linux 2 AMI
data "aws_ami" "amazon_linux" {
  # Most recent AMI
  most_recent = true
  
  # AMI owners (Amazon)
  owners      = ["amazon"]
  
  # Filter for Amazon Linux 2 AMIs
  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
  
  # Filter for EBS-backed AMIs
  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
  
  # Filter for virtualization type
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Create a security group for the EC2 instances
resource "aws_security_group" "instance" {
  # Name of the security group
  name        = "${var.app_name}-${var.environment}-instance-sg"
  
  # Description of the security group
  description = "Security group for EC2 instances"
  
  # VPC ID for the security group
  vpc_id      = var.vpc_id
  
  # Ingress rule for SSH access
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH access"
  }
  
  # Ingress rule for HTTP access
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP access"
  }
  
  # Ingress rule for HTTPS access
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS access"
  }
  
  # Egress rule for all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }
  
  # Tags for the security group
  tags = {
    Name        = "${var.app_name}-${var.environment}-instance-sg"
    Environment = var.environment
  }
}

# Create EC2 instances
resource "aws_instance" "app" {
  # Count based on the number of instances specified
  count = var.instance_count
  
  # AMI ID for the instance
  ami           = data.aws_ami.amazon_linux.id
  
  # Instance type
  instance_type = var.instance_type
  
  # Key name for SSH access
  key_name      = var.key_name
  
  # Subnet ID for the instance
  subnet_id     = var.subnet_ids[count.index % length(var.subnet_ids)]
  
  # Security group IDs for the instance
  vpc_security_group_ids = [aws_security_group.instance.id]
  
  # User data script for instance initialization
  user_data = var.user_data
  
  # Tags for the instance
  tags = {
    Name        = "${var.app_name}-${var.environment}-instance-${count.index + 1}"
    Environment = var.environment
  }
}