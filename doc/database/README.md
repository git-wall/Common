# Database Templates

This directory contains templates for database schema management, migrations, initialization, and backup/restore procedures.

## Contents

### Flyway Migration Scripts
- `V1__initial_schema.sql`: Initial database schema creation
- `V2__sample_data.sql`: Sample data insertion

### Liquibase Changelog Files
- `changelog-master.xml`: Master changelog file that includes all other changelogs
- `changelog-1.0.xml`: Initial schema creation changelog

### Database Initialization Scripts
- `mysql-init.sql`: MySQL database initialization script
- `postgres-init.sql`: PostgreSQL database initialization script

### Backup and Restore Scripts
- `backup-script.bat`: Windows script for database backup
- `restore-script.bat`: Windows script for database restore

## Usage

### Flyway

1. Add Flyway dependency to your Spring Boot project:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>