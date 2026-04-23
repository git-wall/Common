## What is going on with my server?
- Top 10 CPU-hungry processes
```shell
  ps aux --sort=-%cpu | head -n 10
```

- Port is use, how to find it?
```shell
  lsof -i :<port_number>
```

- Kill a process on a specific port
```shell
  kill -9 $(lsof -t -i :<port_number>)
  # -9 is the signal for SIGKILL, which forcefully terminates the process
  # lsof -t -i :<port_number> returns the PID of the process using the specified port
```

## Something broke, how to find out what?
- Check system logs for errors
```shell
  journalctl -xe
  # journalctl -xe shows the system logs with extra details 
  # and in reverse chronological order, which can help identify recent errors 
  # or issues that may have caused the problem.
```
- Log for a service and time (narrow down)
```shell
  journalctl -u <service_name> --since "2024-06-01" --until "2024-06-30"
  # -u <service_name> filters the logs for a specific service
  # --since and --until specify the time range for the logs
  journalctl -u nginx --since "2024-06-01" --until "2024-06-30"
  journalctl -u mysql --since "1 hour ago"
```

- Security: Live failed login attempts
```shell
  tail -f /var/log/auth.log | grep "Failed"
```

## Networking down, how to check?

```shell
  ip addr show
  # Check if the network interfaces are up and have the correct IP addresses assigned.

  ping <destination>
  # Test connectivity to a specific destination (e.g., another server, gateway, or external website).

  traceroute <destination>
  # Trace the route packets take to reach the destination, which can help identify where the connection is failing.

  netstat -tuln
  # Check for open ports and listening services to ensure that necessary services are running and accessible.
  
  ss -tuln
  # Similar to netstat, but faster and more modern, showing listening sockets and their associated processes.
```

- Heal check for a web server
```shell
  curl -I https://example.com
```

-- Capture live network traffic
```shell
  tcpdump -i eth0 port 80 -w cap.pcap
```

## Disk full, what to do?

- Find top 10 largest directories
```shell
  du -sh /* 2>/dev/null | sort -rh | head -n 10
```

- Delete old log files elder than 30 days
```shell
  find /var/log -type f -name "*.log" -mtime +30 -exec rm -f {} \;
  # This command finds log files in /var/log that are older than 30 days and deletes them.
  # -type f specifies that we are looking for files.
  # -name "*.log" filters for files with a .log extension.
  # -mtime +30 selects files modified more than 30 days ago.
  # -exec rm -f {} \; executes the rm command to delete each found file
```

- Check disk usage by file type
```shell
  find / -type f -exec du -h {} + | sort -rh | head -n 20
  # This command finds all files on the system, calculates their disk usage, and lists the top 20 largest files.
  # -type f specifies that we are looking for files.
  # -exec du -h {} + calculates the disk usage of each file in a human-readable format.
  # sort -rh sorts the output in reverse order based on size, and head -n 20 shows the top 20 entries.
```

## Who has access to what?

- Find all SUID files - privilege escalation risk
```shell
  find / -type f -perm -4000 2>/dev/null
  # This command searches for files with the SUID (Set User ID) permission, which allows users to execute the file with the permissions of the file owner.
  # -type f specifies that we are looking for files.
  # -perm -4000 filters for files with the SUID bit set.
  # 2>/dev/null suppresses error messages for directories we don't have permission to access.
```

- Check file ACL permissions
```shell
  getfacl <file_or_directory>
  # This command displays the Access Control List (ACL) permissions for a specified file or directory, showing who has access and what type of access they have.
```

- Recent login history
```shell
  last -a | grep -v reboot | head 20
  # This command shows a list of recent login sessions, including the username, terminal, IP address, and login/logout times.
```

## Server is slow - where you start?
- CPU, memory, I/O snapshot every 1 seconds
```shell
  vmstat 1 5
  # This command provides a snapshot of system performance, including CPU usage, memory usage, and I/O activity, every 1 second for a total of 5 iterations.
```

- CPU usage history - needs sysstat installed
```shell
  sar -u 1 3
  # This command shows CPU usage history, with updates every 1 second for a total of 3 iterations. 
  # It requires the sysstat package to be installed on the system.
```

- Live memory usage every 1 seconds
```shell
  watch -n 1 'ps aux --sort=-%mem | head -10'
  # This command uses watch to execute the ps command every 1 second, showing the top 10 processes sorted by memory usage in real-time.
```