@echo off
REM =====================================================
REM Stadium Management System - Database Setup Script
REM Runs all SQL migration files in order
REM =====================================================

echo ========================================
echo Stadium Management Database Setup
echo ========================================
echo.

REM Check if sqlcmd is available
where sqlcmd >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: sqlcmd not found!
    echo Please install SQL Server Command Line Utilities
    echo Download from: https://docs.microsoft.com/en-us/sql/tools/sqlcmd-utility
    exit /b 1
)

echo [OK] sqlcmd found
echo.

REM Get database connection details
set /p DB_SERVER="Enter SQL Server (e.g., localhost or .\SQLEXPRESS): "
set /p DB_USER="Enter Username (e.g., sa): "
set /p DB_PASSWORD="Enter Password: "
set DB_NAME=StadiumDB

echo.
echo Connecting to: %DB_SERVER%
echo Database: %DB_NAME%
echo.

REM Run each migration file in order
echo [1/6] Running 01_initial_setup.sql...
sqlcmd -S %DB_SERVER% -U %DB_USER% -P %DB_PASSWORD% -i "database\01_initial_setup.sql"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to run 01_initial_setup.sql
    exit /b 1
)
echo [OK] Initial setup completed
echo.

echo [2/6] Running 02_sync_seats.sql...
sqlcmd -S %DB_SERVER% -U %DB_USER% -P %DB_PASSWORD% -d %DB_NAME% -i "database\02_sync_seats.sql"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to run 02_sync_seats.sql
    exit /b 1
)
echo [OK] Sync seats completed
echo.

echo [3/6] Running 03_features_roles_staff_inventory.sql...
sqlcmd -S %DB_SERVER% -U %DB_USER% -P %DB_PASSWORD% -d %DB_NAME% -i "database\03_features_roles_staff_inventory.sql"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to run 03_features_roles_staff_inventory.sql
    exit /b 1
)
echo [OK] Features, roles, staff, and inventory setup completed
echo.

echo [4/6] Running 04_add_inventory_fields.sql...
sqlcmd -S %DB_SERVER% -U %DB_USER% -P %DB_PASSWORD% -d %DB_NAME% -i "database\04_add_inventory_fields.sql"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to run 04_add_inventory_fields.sql
    exit /b 1
)
echo [OK] Inventory fields added
echo.

echo [5/6] Running 06_event_expenses.sql...
sqlcmd -S %DB_SERVER% -U %DB_USER% -P %DB_PASSWORD% -d %DB_NAME% -i "database\06_event_expenses.sql"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to run 06_event_expenses.sql
    exit /b 1
)
echo [OK] Event expenses setup completed
echo.

echo [6/6] Running 07_allow_null_seatid_for_standing_areas.sql...
sqlcmd -S %DB_SERVER% -U %DB_USER% -P %DB_PASSWORD% -d %DB_NAME% -i "database\07_allow_null_seatid_for_standing_areas.sql"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to run 07_allow_null_seatid_for_standing_areas.sql
    exit /b 1
)
echo [OK] Standing areas configuration completed
echo.

echo ========================================
echo SUCCESS! Database setup completed
echo ========================================
echo.
echo Database: %DB_NAME% is ready to use
echo.

pause
