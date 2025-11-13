-- Migration script to update tenant_id from BIGINT to VARCHAR to support UUIDs
-- This allows the payment service to work with UUID-based tenant IDs from core-service

-- Step 1: Alter the customers table to change tenant_id column type
ALTER TABLE customers ALTER COLUMN tenant_id TYPE VARCHAR(255);

-- Step 2: Update any existing records if needed (optional - only if you have test data)
-- This step can be skipped if the table is empty or you want to keep existing data as-is
-- UPDATE customers SET tenant_id = CAST(tenant_id AS VARCHAR) WHERE tenant_id IS NOT NULL;

-- Note: After running this migration, the payment service will accept UUID strings for tenant_id
