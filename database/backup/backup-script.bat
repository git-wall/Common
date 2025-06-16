@echo off
REM Database backup script for Windows

REM Set variables
set BACKUP_DIR=%~dp0backups
set TIMESTAMP=%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set DB_NAME=app_db
set DB_USER=app_user
set DB_PASSWORD=app_password
set DB_HOST=localhost
set DB_PORT=3306

REM Create backup directory if it doesn't exist
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM Choose database type (MySQL or PostgreSQL)
set DB_TYPE=mysql
REM set DB_TYPE=postgresql

if "%DB_TYPE%"=="mysql" (
    REM MySQL backup
    echo Backing up MySQL database %DB_NAME%...
    mysqldump -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% --databases %DB_NAME% --routines --events --triggers > "%BACKUP_DIR%\%DB_NAME%_%TIMESTAMP%.sql"
) else if "%DB_TYPE%"=="postgresql" (
    REM PostgreSQL backup
    echo Backing up PostgreSQL database %DB_NAME%...
    set PGPASSWORD=%DB_PASSWORD%
    pg_dump -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -F c -b -v -f "%BACKUP_DIR%\%DB_NAME%_%TIMESTAMP%.backup" %DB_NAME%
)

REM Check if backup was successful
if %ERRORLEVEL% == 0 (
    echo Backup completed successfully: %BACKUP_DIR%\%DB_NAME%_%TIMESTAMP%.sql
    
    REM Delete backups older than 30 days
    echo Removing backups older than 30 days...
    forfiles /p "%BACKUP_DIR%" /s /m *.* /d -30 /c "cmd /c del @path" 2>nul
    if %ERRORLEVEL% == 0 (
        echo Old backups removed successfully.
    ) else (
        echo No old backups to remove.
    )
) else (
    echo Backup failed with error code %ERRORLEVEL%
)

echo Backup process completed at %date% %time%