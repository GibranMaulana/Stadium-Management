-- ============================================================================
-- Stadium Management System - Feature Enhancement Migration
-- Version: 1.1.0
-- Description: Adds Role-Based Access Control, Staff & Inventory Management
-- ============================================================================

USE StadiumDB;
GO

-- ============================================================================
-- 1. MODIFIKASI TABEL ADMIN (untuk Super Admin)
-- ============================================================================
PRINT 'Adding Role column to Admins table...';

-- Cek apakah kolom Role sudah ada
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Admins') AND name = 'Role')
BEGIN
    ALTER TABLE Admins
    ADD Role VARCHAR(50) NOT NULL DEFAULT 'ADMIN';
    PRINT '✓ Role column added to Admins table';
END
ELSE
BEGIN
    PRINT '⚠ Role column already exists in Admins table';
END
GO

-- Jadikan admin 'admin' sebagai SUPER_ADMIN
UPDATE Admins
SET Role = 'SUPER_ADMIN'
WHERE Username = 'admin';
PRINT '✓ Default admin upgraded to SUPER_ADMIN';
GO

-- ============================================================================
-- 2. TABEL BARU: Staff (Manajemen Staff)
-- ============================================================================
PRINT 'Creating Staff table...';

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID('Staff') AND type = 'U')
BEGIN
    CREATE TABLE Staff (
        StaffID INT PRIMARY KEY IDENTITY(1,1),
        FullName VARCHAR(255) NOT NULL,
        Position VARCHAR(100) NOT NULL,
        Salary DECIMAL(10, 2) NOT NULL,
        PhoneNumber VARCHAR(20),
        Address TEXT,
        HireDate DATE NOT NULL DEFAULT GETDATE(),
        IsActive BIT NOT NULL DEFAULT 1,
        CreatedAt DATETIME DEFAULT GETDATE(),
        UpdatedAt DATETIME DEFAULT GETDATE()
    );
    PRINT '✓ Staff table created';
END
ELSE
BEGIN
    PRINT '⚠ Staff table already exists';
END
GO

-- ============================================================================
-- 3. TABEL BARU: InventoryItems (Inventaris Stadion)
-- ============================================================================
PRINT 'Creating InventoryItems table...';

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID('InventoryItems') AND type = 'U')
BEGIN
    CREATE TABLE InventoryItems (
        ItemID INT PRIMARY KEY IDENTITY(1,1),
        ItemName VARCHAR(255) NOT NULL,
        Description TEXT,
        Quantity INT NOT NULL DEFAULT 0,
        MinStockLevel INT NOT NULL DEFAULT 10,
        Location VARCHAR(100),
        CreatedAt DATETIME DEFAULT GETDATE(),
        UpdatedAt DATETIME DEFAULT GETDATE()
    );
    PRINT '✓ InventoryItems table created';
END
ELSE
BEGIN
    PRINT '⚠ InventoryItems table already exists';
END
GO

-- ============================================================================
-- 4. INSERT SAMPLE DATA
-- ============================================================================
PRINT 'Inserting sample staff data...';

-- Sample Staff
IF NOT EXISTS (SELECT * FROM Staff)
BEGIN
    INSERT INTO Staff (FullName, Position, Salary, PhoneNumber, Address, HireDate)
    VALUES
    ('Budi Santoso', 'Security Manager', 5000000, '081234567890', 'Jakarta Selatan', '2023-01-15'),
    ('Siti Nurhaliza', 'Cleaning Supervisor', 4000000, '081234567891', 'Jakarta Timur', '2023-03-20'),
    ('Ahmad Wijaya', 'Ticketing Staff', 3500000, '081234567892', 'Tangerang', '2023-06-10'),
    ('Dewi Lestari', 'Medical Officer', 6000000, '081234567893', 'Bekasi', '2023-02-28'),
    ('Rudi Hartono', 'Maintenance Head', 5500000, '081234567894', 'Jakarta Barat', '2023-04-05');
    PRINT '✓ Sample staff data inserted';
END
GO

PRINT 'Inserting sample inventory data...';

-- Sample Inventory
IF NOT EXISTS (SELECT * FROM InventoryItems)
BEGIN
    INSERT INTO InventoryItems (ItemName, Description, Quantity, MinStockLevel, Location)
    VALUES
    ('Kursi Lipat Cadangan', 'Untuk area VIP tambahan', 50, 20, 'Gudang A'),
    ('Lampu Sorot LED', 'Cadangan lampu stadion', 10, 5, 'Gudang B'),
    ('Rompi Keamanan', 'Warna oranye reflektif', 200, 50, 'Pos Keamanan'),
    ('Kotak P3K', 'First aid kit lengkap', 15, 10, 'Medical Room'),
    ('Tandu Medis', 'Untuk evakuasi darurat', 5, 3, 'Medical Room'),
    ('Megaphone', 'Pengeras suara portable', 8, 5, 'Security Office'),
    ('Walkie Talkie', 'Radio komunikasi', 25, 15, 'Security Office'),
    ('Fire Extinguisher', 'APAR 3kg', 30, 20, 'Seluruh Area'),
    ('Sapu & Pel', 'Alat kebersihan', 40, 25, 'Cleaning Storage'),
    ('Trash Bags', 'Kantong sampah 100L', 500, 200, 'Cleaning Storage');
    PRINT '✓ Sample inventory data inserted';
END
GO

-- ============================================================================
-- 5. CREATE INDEXES FOR PERFORMANCE
-- ============================================================================
PRINT 'Creating indexes...';

-- Index untuk pencarian staff aktif
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Staff_IsActive' AND object_id = OBJECT_ID('Staff'))
BEGIN
    CREATE INDEX IX_Staff_IsActive ON Staff(IsActive);
    PRINT '✓ Index created on Staff.IsActive';
END
GO

-- Index untuk item inventaris dengan stok rendah
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Inventory_Quantity' AND object_id = OBJECT_ID('InventoryItems'))
BEGIN
    CREATE INDEX IX_Inventory_Quantity ON InventoryItems(Quantity);
    PRINT '✓ Index created on InventoryItems.Quantity';
END
GO

-- ============================================================================
-- 6. CREATE VIEWS FOR REPORTING
-- ============================================================================
PRINT 'Creating reporting views...';

-- View: Staff yang aktif
IF OBJECT_ID('vw_ActiveStaff', 'V') IS NOT NULL
    DROP VIEW vw_ActiveStaff;
GO

CREATE VIEW vw_ActiveStaff AS
SELECT 
    StaffID,
    FullName,
    Position,
    Salary,
    PhoneNumber,
    HireDate,
    DATEDIFF(MONTH, HireDate, GETDATE()) AS MonthsEmployed
FROM Staff
WHERE IsActive = 1;
GO
PRINT '✓ View vw_ActiveStaff created';

-- View: Inventaris dengan stok rendah
IF OBJECT_ID('vw_LowStockItems', 'V') IS NOT NULL
    DROP VIEW vw_LowStockItems;
GO

CREATE VIEW vw_LowStockItems AS
SELECT 
    ItemID,
    ItemName,
    Description,
    Quantity,
    MinStockLevel,
    Location,
    (MinStockLevel - Quantity) AS ShortageAmount
FROM InventoryItems
WHERE Quantity < MinStockLevel;
GO
PRINT '✓ View vw_LowStockItems created';

-- View: Laporan penjualan per event
IF OBJECT_ID('vw_SalesPerEvent', 'V') IS NOT NULL
    DROP VIEW vw_SalesPerEvent;
GO

CREATE VIEW vw_SalesPerEvent AS
SELECT 
    e.EventName,
    e.EventDate,
    COUNT(DISTINCT b.BookingID) AS TotalBookings,
    COUNT(bs.SeatID) AS TotalTicketsSold,
    ISNULL(SUM(es.Price), 0) AS TotalRevenue
FROM Events e
LEFT JOIN EventSections es ON e.EventID = es.EventID
LEFT JOIN Bookings b ON e.EventID = b.EventID
LEFT JOIN BookingSeats bs ON b.BookingID = bs.BookingID AND bs.SectionID = es.SectionID
GROUP BY e.EventID, e.EventName, e.EventDate;
GO
PRINT '✓ View vw_SalesPerEvent created';

PRINT '';
PRINT '============================================================================';
PRINT '✓✓✓ DATABASE MIGRATION COMPLETED SUCCESSFULLY! ✓✓✓';
PRINT '============================================================================';
PRINT '';
PRINT 'Summary:';
PRINT '- Role column added to Admins';
PRINT '- Staff table created with sample data (5 staff)';
PRINT '- InventoryItems table created with sample data (10 items)';
PRINT '- Indexes created for performance';
PRINT '- Reporting views created';
PRINT '';
