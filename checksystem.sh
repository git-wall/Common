#!/usr/bin/env bash

HOSTNAME=$(hostname)
DISK=$(df -h | tail -1 | awl '{print $1}')
CPU=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}')

echo "HOSTNAME: " $HOSTNAME
echo "DISK: " $DISK
echo "CPU: " $CPU


# enable permissions for file checksystem.sh
#chmod +x checksystem.sh
