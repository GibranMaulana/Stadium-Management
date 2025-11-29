USE StadiumDB;
GO

-- Create EventExpenses table (if not exists)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'EventExpenses')
BEGIN
    CREATE TABLE EventExpenses (
        ExpenseID INT IDENTITY(1,1) PRIMARY KEY,
        EventID INT NOT NULL,
        ExpenseType VARCHAR(50) NOT NULL,
        ItemID INT NULL,
        Quantity INT NULL,
        UnitCost DECIMAL(18,2) NULL,
        TotalCost DECIMAL(18,2) NOT NULL,
        Notes NVARCHAR(1000) NULL,
        CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_EventExpenses_Event FOREIGN KEY (EventID) REFERENCES Events(EventID)
    );
    
    CREATE INDEX IDX_EventExpenses_EventID ON EventExpenses(EventID);
    PRINT '✓ EventExpenses table created';
END
ELSE
    PRINT '⚠ EventExpenses table already exists';
GO

-- Create InventoryPurchases table (if not exists)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'InventoryPurchases')
BEGIN
    CREATE TABLE InventoryPurchases (
        PurchaseID INT IDENTITY(1,1) PRIMARY KEY,
        EventID INT NULL,
        ItemID INT NOT NULL,
        Quantity INT NOT NULL,
        UnitCost DECIMAL(18,2) NOT NULL,
        TotalCost DECIMAL(18,2) NOT NULL,
        PurchaseDate DATETIME NOT NULL DEFAULT GETDATE(),
        Supplier NVARCHAR(200) NULL,
        Notes NVARCHAR(1000) NULL,
        CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_InventoryPurchases_Event FOREIGN KEY (EventID) REFERENCES Events(EventID),
        CONSTRAINT FK_InventoryPurchases_Item FOREIGN KEY (ItemID) REFERENCES InventoryItems(ItemID)
    );
    
    CREATE INDEX IDX_InventoryPurchases_EventID ON InventoryPurchases(EventID);
    CREATE INDEX IDX_InventoryPurchases_ItemID ON InventoryPurchases(ItemID);
    PRINT '✓ InventoryPurchases table created';
END
ELSE
    PRINT '⚠ InventoryPurchases table already exists';
GO

PRINT '✅ Database migration completed successfully!';