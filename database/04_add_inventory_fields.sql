-- ============================================================================
-- Stadium Management System - Inventory Enhancement Migration
-- Version: 1.2.0
-- Description: Adds Category and UnitPrice fields to InventoryItems table
-- ============================================================================

USE StadiumDB;
GO

PRINT 'Adding Category and UnitPrice columns to InventoryItems table...';

-- Add Category column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('InventoryItems') AND name = 'Category')
BEGIN
    ALTER TABLE InventoryItems
    ADD Category VARCHAR(100) NULL;
    PRINT '✓ Category column added to InventoryItems table';
END
ELSE
BEGIN
    PRINT '⚠ Category column already exists in InventoryItems table';
END
GO

-- Add UnitPrice column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('InventoryItems') AND name = 'UnitPrice')
BEGIN
    ALTER TABLE InventoryItems
    ADD UnitPrice DECIMAL(12, 2) NOT NULL DEFAULT 0.00;
    PRINT '✓ UnitPrice column added to InventoryItems table';
END
ELSE
BEGIN
    PRINT '⚠ UnitPrice column already exists in InventoryItems table';
END
GO

-- Update existing data with default category
UPDATE InventoryItems
SET Category = 'Other'
WHERE Category IS NULL;
GO

-- Update sample data with realistic prices
UPDATE InventoryItems
SET 
    Category = CASE 
        WHEN ItemName LIKE '%Ball%' THEN 'Sports Equipment'
        WHEN ItemName LIKE '%Net%' THEN 'Sports Equipment'
        WHEN ItemName LIKE '%Water%' OR ItemName LIKE '%Beverage%' THEN 'Food & Beverage'
        WHEN ItemName LIKE '%Clean%' OR ItemName LIKE '%Mop%' OR ItemName LIKE '%Soap%' THEN 'Cleaning Supplies'
        WHEN ItemName LIKE '%Medical%' OR ItemName LIKE '%Aid%' THEN 'Medical Supplies'
        WHEN ItemName LIKE '%Chair%' OR ItemName LIKE '%Table%' THEN 'Furniture'
        ELSE 'Other'
    END,
    UnitPrice = CASE 
        WHEN ItemName LIKE '%Ball%' THEN 150000.00
        WHEN ItemName LIKE '%Net%' THEN 250000.00
        WHEN ItemName LIKE '%Water%' THEN 5000.00
        WHEN ItemName LIKE '%Beverage%' THEN 8000.00
        WHEN ItemName LIKE '%Clean%' THEN 35000.00
        WHEN ItemName LIKE '%Medical%' THEN 50000.00
        WHEN ItemName LIKE '%Chair%' THEN 200000.00
        ELSE 10000.00
    END
WHERE UnitPrice = 0.00;
GO

PRINT '✓ InventoryItems table updated successfully with Category and UnitPrice';
PRINT '';
PRINT '============================================================';
PRINT 'Migration completed! Summary:';
PRINT '- Added Category column (VARCHAR(100))';
PRINT '- Added UnitPrice column (DECIMAL(12,2))';
PRINT '- Updated existing records with default values';
PRINT '============================================================';
GO
