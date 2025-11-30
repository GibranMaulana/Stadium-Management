-- =============================================
-- Stadium Management System - Database Setup
-- Version: 1.0.0
-- Description: Complete database schema with initial data
-- =============================================

USE master;
GO

-- Drop database if exists (for fresh installation)
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'StadiumDB')
BEGIN
    ALTER DATABASE StadiumDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE StadiumDB;
END
GO

-- Create database
CREATE DATABASE StadiumDB;
GO

USE StadiumDB;
GO

PRINT '========================================';
PRINT 'Creating Tables...';
PRINT '========================================';

-- =============================================
-- TABLE: Admins
-- =============================================
CREATE TABLE Admins (
    AdminID INT PRIMARY KEY IDENTITY(1,1),
    Username NVARCHAR(50) NOT NULL UNIQUE,
    Password NVARCHAR(255) NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

-- =============================================
-- TABLE: Sections
-- =============================================
CREATE TABLE Sections (
    SectionID INT PRIMARY KEY IDENTITY(1,1),
    SectionName NVARCHAR(100) NOT NULL,
    SectionType NVARCHAR(20) NOT NULL CHECK (SectionType IN ('TRIBUNE', 'FIELD')),
    TotalRows INT NOT NULL DEFAULT 0,
    SeatsPerRow INT NOT NULL DEFAULT 0,
    Description NVARCHAR(500),
    IsActive BIT DEFAULT 1,
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE()
);
GO

-- =============================================
-- TABLE: Seats
-- =============================================
CREATE TABLE Seats (
    SeatID INT PRIMARY KEY IDENTITY(1,1),
    SectionID INT NOT NULL,
    RowNumber NVARCHAR(10) NOT NULL,
    SeatNumber INT NOT NULL,
    IsActive BIT DEFAULT 1,
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (SectionID) REFERENCES Sections(SectionID) ON DELETE CASCADE,
    CONSTRAINT UQ_Seat UNIQUE (SectionID, RowNumber, SeatNumber)
);
GO

-- =============================================
-- TABLE: Events
-- =============================================
CREATE TABLE Events (
    EventID INT PRIMARY KEY IDENTITY(1,1),
    EventName NVARCHAR(200) NOT NULL,
    EventType NVARCHAR(50) NOT NULL CHECK (EventType IN ('Football', 'Concert')),
    EventDate DATE NOT NULL,
    EventTime TIME NOT NULL,
    Description NVARCHAR(1000),
    Status NVARCHAR(20) DEFAULT 'Active' CHECK (Status IN ('Active', 'Cancelled', 'Completed')),
    TotalSeats INT DEFAULT 0,
    BookedSeats INT DEFAULT 0,
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE()
);
GO

-- =============================================
-- TABLE: EventSections
-- =============================================
CREATE TABLE EventSections (
    EventSectionID INT PRIMARY KEY IDENTITY(1,1),
    EventID INT NOT NULL,
    SectionID INT NOT NULL,
    SectionTitle NVARCHAR(100) NOT NULL,
    Price DECIMAL(10,2) NOT NULL,
    TotalCapacity INT NOT NULL,
    AvailableCapacity INT NOT NULL,
    IsActive BIT DEFAULT 1,
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (EventID) REFERENCES Events(EventID) ON DELETE CASCADE,
    FOREIGN KEY (SectionID) REFERENCES Sections(SectionID),
    CONSTRAINT UQ_EventSection UNIQUE (EventID, SectionID)
);
GO

-- =============================================
-- TABLE: Bookings
-- =============================================
CREATE TABLE Bookings (
    BookingID INT PRIMARY KEY IDENTITY(1,1),
    EventID INT NOT NULL,
    BookingNumber NVARCHAR(50) NOT NULL UNIQUE,
    CustomerName NVARCHAR(200) NOT NULL,
    CustomerEmail NVARCHAR(200),
    CustomerPhone NVARCHAR(20),
    TotalSeats INT NOT NULL,
    TotalPrice DECIMAL(10,2) NOT NULL,
    BookingStatus NVARCHAR(20) DEFAULT 'Confirmed' CHECK (BookingStatus IN ('Confirmed', 'Cancelled')),
    PaymentStatus NVARCHAR(20) DEFAULT 'Paid' CHECK (PaymentStatus IN ('Paid', 'Pending', 'Refunded')),
    BookingDate DATETIME DEFAULT GETDATE(),
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (EventID) REFERENCES Events(EventID)
);
GO

-- =============================================
-- TABLE: BookingSeats
-- =============================================
CREATE TABLE BookingSeats (
    BookingSeatID INT PRIMARY KEY IDENTITY(1,1),
    BookingID INT NOT NULL,
    EventID INT NOT NULL,
    SectionID INT NOT NULL,
    SeatID INT NOT NULL,
    RowNumber NVARCHAR(10) NOT NULL,
    SeatNumber INT NOT NULL,
    Price DECIMAL(10,2) NOT NULL,
    Status NVARCHAR(20) DEFAULT 'Booked',
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID) ON DELETE CASCADE,
    FOREIGN KEY (EventID) REFERENCES Events(EventID),
    FOREIGN KEY (SectionID) REFERENCES Sections(SectionID),
    FOREIGN KEY (SeatID) REFERENCES Seats(SeatID),
    CONSTRAINT UQ_BookingSeat UNIQUE (BookingID, SeatID)
);
GO

PRINT 'Tables created successfully!';
PRINT '';

-- =============================================
-- INSERT INITIAL DATA
-- =============================================
PRINT '========================================';
PRINT 'Inserting Initial Data...';
PRINT '========================================';

-- Admin user
INSERT INTO Admins (Username, Password) VALUES ('admin', 'admin123');
PRINT 'Admin user created (username: admin, password: admin123)';

-- Tribune Sections
INSERT INTO Sections (SectionName, SectionType, TotalRows, SeatsPerRow, Description) VALUES
('Tribun Utara', 'TRIBUNE', 30, 50, 'North Tribune - Available for Football Matches & Concerts'),
('Tribun Barat', 'TRIBUNE', 30, 50, 'West Tribune - Available for Football Matches & Concerts'),
('Tribun Timur', 'TRIBUNE', 30, 50, 'East Tribune - Available for Football Matches & Concerts'),
('Tribun Selatan', 'TRIBUNE', 30, 50, 'South Tribune - Available for Football Matches & Concerts');
PRINT '4 Tribune sections created (1,500 seats each)';

-- Field Zones
INSERT INTO Sections (SectionName, SectionType, TotalRows, SeatsPerRow, Description) VALUES
('Field Zone A', 'FIELD', 0, 0, 'Standing Area - Available for Concerts Only'),
('Field Zone B', 'FIELD', 0, 0, 'Standing Area - Available for Concerts Only');
PRINT '2 Field zones created (standing areas)';

PRINT '';
PRINT '========================================';
PRINT 'DATABASE SETUP COMPLETE!';
PRINT '========================================';
PRINT 'Next steps:';
PRINT '1. Run sync_seats.sql to generate seat records';
PRINT '2. Update your .env file with database credentials';
PRINT '3. Run the application: mvn javafx:run';
PRINT '';

-- Display summary
SELECT 
    'Sections Created' AS Summary,
    COUNT(*) AS Count
FROM Sections;

GO
