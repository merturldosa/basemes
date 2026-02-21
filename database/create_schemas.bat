@echo off
REM ============================================================================
REM SDS MES - Create Database Schemas
REM Automatically finds psql and creates required schemas
REM ============================================================================

echo.
echo ========================================
echo SDS MES - Schema Creation Script
echo ========================================
echo.

REM Database connection parameters
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=sds_mes_dev
set DB_USER=mes_admin
set DB_PASS=mes_password_2024

REM Try to find psql.exe in common locations
set PSQL_PATH=

REM Check if psql is in PATH
where psql >nul 2>&1
if %errorlevel% equ 0 (
    set PSQL_PATH=psql
    echo [INFO] Found psql in system PATH
    goto :run_sql
)

REM Check common PostgreSQL installation paths
if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" (
    set PSQL_PATH="C:\Program Files\PostgreSQL\16\bin\psql.exe"
    echo [INFO] Found psql at: C:\Program Files\PostgreSQL\16\bin\psql.exe
    goto :run_sql
)

if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" (
    set PSQL_PATH="C:\Program Files\PostgreSQL\15\bin\psql.exe"
    echo [INFO] Found psql at: C:\Program Files\PostgreSQL\15\bin\psql.exe
    goto :run_sql
)

if exist "C:\Program Files\PostgreSQL\14\bin\psql.exe" (
    set PSQL_PATH="C:\Program Files\PostgreSQL\14\bin\psql.exe"
    echo [INFO] Found psql at: C:\Program Files\PostgreSQL\14\bin\psql.exe
    goto :run_sql
)

if exist "C:\PostgreSQL\16\bin\psql.exe" (
    set PSQL_PATH="C:\PostgreSQL\16\bin\psql.exe"
    echo [INFO] Found psql at: C:\PostgreSQL\16\bin\psql.exe
    goto :run_sql
)

if exist "C:\PostgreSQL\15\bin\psql.exe" (
    set PSQL_PATH="C:\PostgreSQL\15\bin\psql.exe"
    echo [INFO] Found psql at: C:\PostgreSQL\15\bin\psql.exe
    goto :run_sql
)

REM If we get here, psql was not found
echo [ERROR] Could not find psql.exe
echo.
echo Please use one of these methods instead:
echo.
echo 1. Manual SQL execution in DBeaver/pgAdmin:
echo    - Open database/create_schemas.sql
echo    - Execute the entire script
echo.
echo 2. Add PostgreSQL to your PATH and run this script again
echo.
echo 3. See database/README_SCHEMA_SETUP.md for detailed instructions
echo.
pause
exit /b 1

:run_sql
echo.
echo [INFO] Executing schema creation script...
echo [INFO] Database: %DB_NAME%
echo [INFO] User: %DB_USER%
echo.

REM Set password as environment variable to avoid prompt
set PGPASSWORD=%DB_PASS%

REM Run the SQL script
%PSQL_PATH% -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f create_schemas.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo [SUCCESS] All schemas created successfully!
    echo ========================================
    echo.
    echo You can now start the backend application:
    echo   cd backend
    echo   java -jar target/sds-mes-backend-0.0.1-SNAPSHOT.jar
    echo.
) else (
    echo.
    echo ========================================
    echo [ERROR] Failed to create schemas
    echo ========================================
    echo.
    echo Please check:
    echo 1. Database connection parameters
    echo 2. PostgreSQL is running
    echo 3. Database 'sds_mes_dev' exists
    echo.
    echo See database/README_SCHEMA_SETUP.md for manual setup
    echo.
)

REM Clear password from environment
set PGPASSWORD=

pause
