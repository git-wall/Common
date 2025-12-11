#!/usr/bin/env bash

HOSTNAME=$(hostname)

# df(disk free)
# top(cpu usage)
# awk: pattern scanning and processing language
# grep: search for a specific pattern in a file or output
# tail: output the last part of files
# cut: remove sections from each line of files
# df -h: display disk space usage in human-readable format
# top -bn1: display CPU usage in batch mode, one iteration
# awk '{print $1}': print the first column of the output
# awk '{print $2}': print the second column of the output
# tail -1: output the last line of the output
# grep "Cpu(s)": filter the output to show only CPU usage information
# awk '{print $2}': print the second column of the output
DISK=$(df -h | tail -1 | awk '{print $1}')
CPU=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}')

echo "HOSTNAME: " "$HOSTNAME"
echo "DISK: " "$DISK"
echo "CPU: " "$CPU"

chmod +x /pipeline.sh
echo "Running pipeline container script..."
