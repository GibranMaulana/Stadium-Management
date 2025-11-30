#!/bin/bash
# =====================================================
# Stadium Management System - Database Setup Script
# Runs all SQL migration files in order
# =====================================================

echo "========================================"
echo "Stadium Management Database Setup"
echo "========================================"
echo ""

# Check if sqlcmd is available
if ! command -v sqlcmd &> /dev/null; then
    echo "ERROR: sqlcmd not found!"
    echo "Please install SQL Server Command Line Utilities"
    echo "Download from: https://docs.microsoft.com/en-us/sql/tools/sqlcmd-utility"
    exit 1
fi

echo "[OK] sqlcmd found"
echo ""

# Get database connection details
read -p "Enter SQL Server (e.g., localhost): " DB_SERVER
read -p "Enter Username (e.g., sa): " DB_USER
read -sp "Enter Password: " DB_PASSWORD
echo ""
DB_NAME="StadiumDB"

echo ""
echo "Connecting to: $DB_SERVER"
echo "Database: $DB_NAME"
echo ""

# Run each migration file in order
echo "[1/6] Running 01_initial_setup.sql..."
sqlcmd -S "$DB_SERVER" -U "$DB_USER" -P "$DB_PASSWORD" -i "database/01_initial_setup.sql"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run 01_initial_setup.sql"
    exit 1
fi
echo "[OK] Initial setup completed"
echo ""

echo "[2/6] Running 02_sync_seats.sql..."
sqlcmd -S "$DB_SERVER" -U "$DB_USER" -P "$DB_PASSWORD" -d "$DB_NAME" -i "database/02_sync_seats.sql"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run 02_sync_seats.sql"
    exit 1
fi
echo "[OK] Sync seats completed"
echo ""

echo "[3/6] Running 03_features_roles_staff_inventory.sql..."
sqlcmd -S "$DB_SERVER" -U "$DB_USER" -P "$DB_PASSWORD" -d "$DB_NAME" -i "database/03_features_roles_staff_inventory.sql"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run 03_features_roles_staff_inventory.sql"
    exit 1
fi
echo "[OK] Features, roles, staff, and inventory setup completed"
echo ""

echo "[4/6] Running 04_add_inventory_fields.sql..."
sqlcmd -S "$DB_SERVER" -U "$DB_USER" -P "$DB_PASSWORD" -d "$DB_NAME" -i "database/04_add_inventory_fields.sql"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run 04_add_inventory_fields.sql"
    exit 1
fi
echo "[OK] Inventory fields added"
echo ""

echo "[5/6] Running 06_event_expenses.sql..."
sqlcmd -S "$DB_SERVER" -U "$DB_USER" -P "$DB_PASSWORD" -d "$DB_NAME" -i "database/06_event_expenses.sql"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run 06_event_expenses.sql"
    exit 1
fi
echo "[OK] Event expenses setup completed"
echo ""

echo "[6/6] Running 07_allow_null_seatid_for_standing_areas.sql..."
sqlcmd -S "$DB_SERVER" -U "$DB_USER" -P "$DB_PASSWORD" -d "$DB_NAME" -i "database/07_allow_null_seatid_for_standing_areas.sql"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to run 07_allow_null_seatid_for_standing_areas.sql"
    exit 1
fi
echo "[OK] Standing areas configuration completed"
echo ""

echo "========================================"
echo "SUCCESS! Database setup completed"
echo "========================================"
echo ""
echo "Database: $DB_NAME is ready to use"
echo ""
