-- Migration: 06_event_expenses.sql
-- Adds table to store event-level expenses (operational costs, inventory damage, etc.)
CREATE TABLE EventExpenses (
    ExpenseID INT IDENTITY(1,1) PRIMARY KEY,
    EventID INT NOT NULL,
    ExpenseType VARCHAR(50) NOT NULL, -- e.g., 'INVENTORY_DAMAGE', 'OPERATIONAL'
    ItemID INT NULL,
    Quantity INT NULL,
    UnitCost DECIMAL(18,2) NULL,
    TotalCost DECIMAL(18,2) NOT NULL,
    Notes NVARCHAR(1000) NULL,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_EventExpenses_Event FOREIGN KEY (EventID) REFERENCES Events(EventID)
);

-- Optional index to support queries by EventID
CREATE INDEX IDX_EventExpenses_EventID ON EventExpenses(EventID);
