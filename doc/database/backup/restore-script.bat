@echo off
REM Database restore script for Windows

REM Set variables
set DB_NAME=app_db
set DB_USER=app_user
set DB_PASSWORD=app_password
set DB_HOST=localhost
set DB_PORT=3306

REM Choose database type (MySQL or PostgreSQL)
set DB_TYPE=mysql
REM set DB_TYPE=postgresql

REM Get backup file path from command line argument
set BACKUP_FILE=%1

if "%BACKUP_FILE%"=="" (
    echo Please provide the backup file path as an argument.
    echo Usage: restore-script.bat path\to\backup\file
    exit /b 1
)

if not exist "%BACKUP_FILE%" (
    echo Backup file not found: %BACKUP_FILE%
    exit /b 1
)

echo Restoring database %DB_NAME% from %BACKUP_FILE%...

if "%DB_TYPE%"=="mysql" (
    REM MySQL restore
    echo Restoring MySQL database...
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% < "%BACKUP_FILE%"
) else if "%DB_TYPE%"=="postgresql" (
    REM PostgreSQL restore
    echo Restoring PostgreSQL database...
    set PGPASSWORD=%DB_PASSWORD%
    
    REM Drop existing connections
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='%DB_NAME%' AND pid <> pg_backend_pid();"
    
    REM Drop and recreate the database
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d postgres -c "DROP DATABASE IF EXISTS %DB_NAME%;"
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d postgres -c "CREATE DATABASE %DB_NAME%;"
    
    REM Restore from backup
    pg_restore -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -v "%BACKUP_FILE%"
)

REM Check if restore was successful
if %ERRORLEVEL% == 0 (
    echo Database restored successfully.
) else (
    echo Restore failed with error code %ERRORLEVEL%
)

echo Restore process completed at %date% %time%