# Database Schema Setup Instructions

## Problem
The backend application failed to start with the error:
```
org.postgresql.util.PSQLException: ERROR: schema "business" does not exist
```

This happens because the PostgreSQL database exists, but the required schemas haven't been created yet.

## Solution Options

### Option 1: Using psql Command Line (Fastest)

If you have PostgreSQL installed, run:

```bash
psql -h localhost -U mes_admin -d soice_mes_dev -f database/create_schemas.sql
```

When prompted, enter the password: `mes_password_2024`

### Option 2: Using DBeaver or pgAdmin (Recommended if psql not available)

1. Open DBeaver or pgAdmin
2. Connect to the database:
   - Host: localhost
   - Port: 5432
   - Database: soice_mes_dev
   - Username: mes_admin
   - Password: mes_password_2024

3. Open the SQL script:
   - File: `database/create_schemas.sql`

4. Execute the entire script

5. Verify schemas were created:
   ```sql
   SELECT schema_name
   FROM information_schema.schemata
   WHERE schema_name IN ('common', 'core', 'mes', 'business', 'inventory', 'bom', 'material', 'purchase', 'sales', 'qms', 'wms', 'equipment')
   ORDER BY schema_name;
   ```

### Option 3: Manual SQL Execution

If you prefer, copy and paste this into your SQL client:

```sql
-- Create all required schemas
CREATE SCHEMA IF NOT EXISTS common;
CREATE SCHEMA IF NOT EXISTS core;
CREATE SCHEMA IF NOT EXISTS mes;
CREATE SCHEMA IF NOT EXISTS business;
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS bom;
CREATE SCHEMA IF NOT EXISTS material;
CREATE SCHEMA IF NOT EXISTS purchase;
CREATE SCHEMA IF NOT EXISTS sales;
CREATE SCHEMA IF NOT EXISTS qms;
CREATE SCHEMA IF NOT EXISTS wms;
CREATE SCHEMA IF NOT EXISTS equipment;

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA common TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA core TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA mes TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA business TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA inventory TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA bom TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA material TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA purchase TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA sales TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA qms TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA wms TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA equipment TO mes_admin;
```

## Required Schemas

The following schemas are needed by the application:

| Schema | Entity Count | Purpose |
|--------|-------------|---------|
| common | 25 | Common tables (users, roles, tenants, etc.) |
| wms | 11 | Warehouse Management System |
| mes | 7 | Manufacturing Execution System core |
| equipment | 7 | Equipment management |
| core | 6 | Core business entities |
| qms | 5 | Quality Management System |
| sales | 4 | Sales management |
| inventory | 4 | Inventory management |
| purchase | 3 | Purchase management |
| business | 2 | Customers and suppliers |
| bom | 2 | Bill of Materials |
| material | 1 | Material management |

## After Creating Schemas

1. Restart the backend application:
   ```bash
   cd backend
   java -jar target/soice-mes-backend-0.0.1-SNAPSHOT.jar
   ```

2. The application should start successfully

3. Hibernate will automatically create all tables in the appropriate schemas (ddl-auto: update mode)

## Troubleshooting

If you still get errors after creating schemas:

1. **Check if schemas exist:**
   ```sql
   SELECT schema_name FROM information_schema.schemata ORDER BY schema_name;
   ```

2. **Check permissions:**
   ```sql
   SELECT * FROM information_schema.role_table_grants
   WHERE grantee = 'mes_admin'
   LIMIT 10;
   ```

3. **Verify database connection in application.yml:**
   - URL: jdbc:postgresql://localhost:5432/soice_mes_dev
   - Username: mes_admin
   - Password: mes_password_2024

## Notes

- The application uses `ddl-auto: update` mode, so Hibernate will create tables automatically
- However, Hibernate does NOT create schemas - they must be created manually first
- Once schemas are created, you don't need to run migrations manually (Hibernate handles it)
