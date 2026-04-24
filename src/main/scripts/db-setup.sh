#!/usr/bin/env sh
#Run "sh src/main/scripts/db-setup.sh" from project root

CONFIG_FILE="src/main/resources/config.properties"

if [ ! -f "$CONFIG_FILE" ]; then
  echo "Missing $CONFIG_FILE"
  exit 1
fi

DB_HOST=$(grep '^db.host=' "$CONFIG_FILE" | cut -d'=' -f2-)
DB_PORT=$(grep '^db.port=' "$CONFIG_FILE" | cut -d'=' -f2-)
DB_NAME=$(grep '^db.name=' "$CONFIG_FILE" | cut -d'=' -f2-)
DB_USER=$(grep '^db.user=' "$CONFIG_FILE" | cut -d'=' -f2-)
DB_PASSWORD=$(grep '^db.password=' "$CONFIG_FILE" | cut -d'=' -f2-)

export PGPASSWORD="$DB_PASSWORD"

if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" | grep -q 1; then
  createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
fi

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f src/main/resources/schema.sql
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f src/main/resources/seed.sql

echo "Database setup complete."