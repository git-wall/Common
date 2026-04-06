# Bash & Linux Cheat Sheet

## What is Bash/Linux?

### Bash
- **Definition**: Bash (Bourne Again SHell) is a command-line interpreter and scripting language for Unix-like operating systems.
- **Purpose**: It's the default shell for most Linux distributions and macOS (until Catalina).
- **Origin**: Created by Brian Fox in 1989 as a free replacement for the Bourne Shell.

### Linux
- **Definition**: Linux is a family of open-source Unix-like operating systems based on the Linux kernel.
- **Purpose**: Powers everything from embedded devices to supercomputers, servers, and desktop computers.
- **Origin**: Created by Linus Torvalds in 1991.

## How to Use Bash/Linux

### Basic Navigation
```bash
pwd                     # Print working directory
ls                      # List files and directories
ls -la                  # List all files (including hidden) with details
cd directory            # Change to specified directory
cd ..                   # Move up one directory
cd ~                    # Go to home directory
cd -                    # Go to previous directory
```

### File Operations
```bash
touch file.txt          # Create empty file or update timestamp
mkdir directory         # Create directory
mkdir -p dir1/dir2      # Create nested directories
cp source dest          # Copy file or directory
cp -r source dest       # Copy directory recursively
mv source dest          # Move/rename file or directory
rm file                 # Remove file
rm -r directory         # Remove directory recursively
rm -f file              # Force remove file without confirmation
rm -rf directory        # Force remove directory recursively (use with caution!)
```

### File Viewing
```bash
cat file.txt            # Display file contents
less file.txt           # View file with pagination
head file.txt           # Show first 10 lines
head -n 20 file.txt     # Show first 20 lines
tail file.txt           # Show last 10 lines
tail -n 20 file.txt     # Show last 20 lines
tail -f file.txt        # Follow file updates in real-time
```

### Text Processing
```bash
grep pattern file       # Search for pattern in file
grep -r pattern dir     # Search recursively in directory
grep -i pattern file    # Case-insensitive search
grep -v pattern file    # Show lines NOT matching pattern
sed 's/old/new/g' file  # Replace text in file
awk '{print $1}' file   # Print first column of file
sort file               # Sort lines alphabetically
uniq file               # Remove duplicate adjacent lines
wc file                 # Count lines, words, and characters
```

### File Permissions
```bash
chmod 755 file          # Set permissions (rwx for owner, rx for group/others)
chmod +x file           # Make file executable
chmod -R 755 directory  # Set permissions recursively
chown user:group file   # Change file owner and group
```

### Process Management
```bash
ps                      # Show running processes
ps aux                  # Show all processes in detail
top                     # Interactive process viewer
htop                    # Enhanced interactive process viewer
kill PID                # Terminate process by ID
kill -9 PID             # Force terminate process
pkill name              # Kill process by name
bg                      # Send process to background
fg                      # Bring process to foreground
jobs                    # List background jobs
nohup command &         # Run command immune to hangups
```

### System Information
```bash
uname -a                # Show system information
df -h                   # Show disk usage (human-readable)
du -sh directory        # Show directory size
free -h                 # Show memory usage
lscpu                   # Display CPU information
lsusb                   # List USB devices
lspci                   # List PCI devices
ip addr                 # Show network interfaces
```

### Networking
```bash
ping host               # Test network connectivity
curl url                # Transfer data from/to server
wget url                # Download files
ssh user@host           # Connect to remote host via SSH
scp file user@host:path # Secure copy file to remote host
netstat -tuln           # Show listening ports
ifconfig                # Show network interface configuration
ip addr                 # Modern alternative to ifconfig
```

### Package Management (Debian/Ubuntu)
```bash
apt update              # Update package lists
apt upgrade             # Upgrade installed packages
apt install package     # Install package
apt remove package      # Remove package
apt search keyword      # Search for package
dpkg -i package.deb     # Install local .deb package
```

### Package Management (Red Hat/Fedora)
```bash
dnf update              # Update package lists and upgrade
dnf install package     # Install package
dnf remove package      # Remove package
dnf search keyword      # Search for package
rpm -i package.rpm      # Install local .rpm package
```

### Bash Scripting Basics
```bash
#!/bin/bash             # Shebang line (first line of script)
# Comment               # Comments start with #
var="value"             # Variable assignment (no spaces around =)
echo $var               # Variable reference
echo "${var}text"       # Variable with adjacent text
$1, $2, ...             # Command line arguments
$0                      # Script name
$#                      # Number of arguments
$@                      # All arguments
$?                      # Exit status of last command
if [ condition ]; then  # If statement
  commands
elif [ condition ]; then
  commands
else
  commands
fi
for i in {1..5}; do     # For loop
  commands
done
while [ condition ]; do # While loop
  commands
done
function name() {       # Function definition
  commands
  return value
}
```

## Common Real-World Examples

### Web Server Setup
```bash
# Install and configure Apache web server
sudo apt update
sudo apt install apache2
sudo systemctl enable apache2
sudo systemctl start apache2

# Configure firewall
sudo ufw allow 'Apache'
sudo ufw enable

# Create virtual host
sudo mkdir -p /var/www/example.com
sudo chown -R $USER:$USER /var/www/example.com
sudo chmod -R 755 /var/www/example.com

# Create a simple index page
echo "<html><body><h1>Hello World!</h1></body></html>" > /var/www/example.com/index.html
```

### Automated Backup
```bash
#!/bin/bash
# Simple backup script

# Configuration
BACKUP_DIR="/backup"
SOURCE_DIR="/var/www"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_DIR/backup_$TIMESTAMP.tar.gz"

# Create backup directory if it doesn't exist
mkdir -p $BACKUP_DIR

# Create backup
tar -czf $BACKUP_FILE $SOURCE_DIR

# Keep only the 5 most recent backups
ls -t $BACKUP_DIR/backup_*.tar.gz | tail -n +6 | xargs -r rm

# Log the backup
echo "Backup completed: $BACKUP_FILE" >> $BACKUP_DIR/backup.log
```

### Log Analysis
```bash
# Find the top 10 IP addresses accessing your web server
cat /var/log/apache2/access.log | awk '{print $1}' | sort | uniq -c | sort -nr | head -10

# Find all 404 errors
grep "HTTP/1.1\" 404" /var/log/apache2/access.log

# Monitor log file in real-time for errors
tail -f /var/log/apache2/error.log | grep "ERROR"
```

### System Monitoring Script
```bash
#!/bin/bash
# Simple system monitoring script

echo "=== System Information ==="
hostname
uptime
echo

echo "=== CPU Usage ==="
top -bn1 | head -20
echo

echo "=== Memory Usage ==="
free -h
echo

echo "=== Disk Usage ==="
df -h
echo

echo "=== Network Connections ==="
netstat -tuln
echo

echo "=== Last 5 Logins ==="
last | head -5
```

### Batch File Processing
```bash
#!/bin/bash
# Process all CSV files in a directory

INPUT_DIR="/data/input"
OUTPUT_DIR="/data/processed"

# Create output directory if it doesn't exist
mkdir -p $OUTPUT_DIR

# Process each CSV file
for file in $INPUT_DIR/*.csv; do
  filename=$(basename "$file")
  echo "Processing $filename..."
  
  # Example processing: convert to uppercase and count lines
  cat "$file" | tr '[:lower:]' '[:upper:]' > "$OUTPUT_DIR/$filename"
  lines=$(wc -l < "$OUTPUT_DIR/$filename")
  
  echo "  Processed $lines lines"
done

echo "All files processed successfully!"
```

### Docker Container Management
```bash
# List running containers
docker ps

# Stop all running containers
docker stop $(docker ps -q)

# Remove all stopped containers
docker container prune -f

# Build and run a Docker container
docker build -t myapp .
docker run -d -p 8080:80 --name myapp_instance myapp

# View container logs
docker logs -f myapp_instance
```

### Git Workflow
```bash
# Clone repository
git clone https://github.com/username/repo.git
cd repo

# Create and switch to new branch
git checkout -b feature-branch

# Make changes and commit
git add .
git commit -m "Implement new feature"

# Push to remote
git push origin feature-branch

# Update from remote and merge changes
git checkout main
git pull
git merge feature-branch
git push
```

### Continuous Integration Script
```bash
#!/bin/bash
# Simple CI script

# Update code
git pull

# Install dependencies
npm install

# Run tests
npm test
if [ $? -ne 0 ]; then
  echo "Tests failed! Aborting deployment."
  exit 1
fi

# Build application
npm run build

# Deploy
rsync -avz --delete dist/ user@server:/var/www/app/
ssh user@server 'sudo systemctl restart nginx'

echo "Deployment completed successfully!"
```