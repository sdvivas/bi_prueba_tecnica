#!/bin/bash
SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
SERVER="sqlserver"
USER="sa"
PASS="${MSSQL_SA_PASSWORD}"

DB_EXISTS=$($SQLCMD -S $SERVER -U $USER -P $PASS -C -h -1 -Q "SET NOCOUNT ON; SELECT CASE WHEN DB_ID('novobanco') IS NOT NULL THEN 1 ELSE 0 END")

if echo "$DB_EXISTS" | grep -q "1"; then
    echo "Database 'novobanco' already exists. Skipping initialization."
    exit 0
fi

echo "Creating database 'novobanco'..."
$SQLCMD -S $SERVER -U $USER -P $PASS -C -Q "CREATE DATABASE novobanco"

echo "Running schema.sql..."
$SQLCMD -S $SERVER -U $USER -P $PASS -C -d novobanco -i /scripts/schema.sql

echo "Running data.sql..."
$SQLCMD -S $SERVER -U $USER -P $PASS -C -d novobanco -i /scripts/data.sql

echo "Database initialization complete."
