#!/usr/bin/env bash

HOSTNAME=$(hostname)
# shellcheck disable=SC2016
DISK=$(df -h | tail -1 | awl '{print $1}')
CPU=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}')

echo "HOSTNAME: " "$HOSTNAME"
# shellcheck disable=SC2086
echo "DISK: " $DISK
# shellcheck disable=SC2086
echo "CPU: " $CPU


# enable permissions for file checksystem.sh
#chmod +x checksystem.sh
