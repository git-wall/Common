### Disable Root SSH

PermitRootLogin no
Edit `/etc/ssh/sshd_config` then
```shell
  systemctl restart sshd
```

### Update everything

```shell
  apt update && apt upgrade -y
```

### Lock down the firewall

```shell
  firewall-cmd --permanent --add-service=ssh
  firewall-cmd --reload
```
- --permanent makes it survive reboots

### Install Fail2Ban

```shell
  apt install fail2ban -y
  systemctl enable --now fail2ban
```

### Enable Audit Logging

```shell
  apt install auditd -y
  systemctl enable --now auditd
  ausearch -m avc
```
- /var/log/audit/audit.log is the log file for auditd
- ausearch -m avc searches for SELinux denials in the audit log