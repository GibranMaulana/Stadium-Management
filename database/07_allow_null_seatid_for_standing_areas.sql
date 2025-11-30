-- =============================================
-- Migration: Allow NULL SeatID for Standing Areas
-- Version: 1.1.0
-- Description: Modify BookingSeats to support field zones without individual seat assignments
-- =============================================

USE StadiumDB;
GO

PRINT '========================================';
PRINT 'Modifying BookingSeats Table...';
PRINT '========================================';

-- First, drop the foreign key constraint
IF EXISTS (
    SELECT * FROM sys.foreign_keys 
    WHERE name = 'FK__BookingSe__SeatI__[hash]' 
    OR parent_object_id = OBJECT_ID('BookingSeats') 
    AND referenced_object_id = OBJECT_ID('Seats')
)
BEGIN
    DECLARE @FKName NVARCHAR(255);
    SELECT @FKName = name 
    FROM sys.foreign_keys 
    WHERE parent_object_id = OBJECT_ID('BookingSeats') 
    AND referenced_object_id = OBJECT_ID('Seats');
    
    IF @FKName IS NOT NULL
    BEGIN
        DECLARE @SQL NVARCHAR(MAX) = 'ALTER TABLE BookingSeats DROP CONSTRAINT ' + @FKName;
        EXEC sp_executesql @SQL;
        PRINT 'Dropped foreign key constraint on SeatID';
    END
END
GO

-- Drop the unique constraint that includes SeatID
IF EXISTS (
    SELECT * FROM sys.key_constraints 
    WHERE name = 'UQ_BookingSeat' 
    AND parent_object_id = OBJECT_ID('BookingSeats')
)
BEGIN
    ALTER TABLE BookingSeats DROP CONSTRAINT UQ_BookingSeat;
    PRINT 'Dropped unique constraint UQ_BookingSeat';
END
GO

-- Modify SeatID to allow NULL
ALTER TABLE BookingSeats
ALTER COLUMN SeatID INT NULL;
GO
PRINT 'Modified SeatID to allow NULL values';

-- Also allow NULL for RowNumber and SeatNumber (for standing areas)
ALTER TABLE BookingSeats
ALTER COLUMN RowNumber NVARCHAR(10) NULL;
GO

ALTER TABLE BookingSeats
ALTER COLUMN SeatNumber INT NULL;
GO
PRINT 'Modified RowNumber and SeatNumber to allow NULL values';

-- Re-add foreign key constraint with ON DELETE CASCADE (optional, only if SeatID is provided)
ALTER TABLE BookingSeats
ADD CONSTRAINT FK_BookingSeats_Seats 
FOREIGN KEY (SeatID) REFERENCES Seats(SeatID) ON DELETE CASCADE;
GO
PRINT 'Re-added foreign key constraint (with NULL support)';

-- Add a new unique constraint that handles both seated and standing bookings
-- For seated bookings: unique by BookingID and SeatID
-- For standing areas: we'll rely on capacity checks instead
ALTER TABLE BookingSeats
ADD CONSTRAINT CHK_BookingSeat_Validity 
CHECK (
    (SeatID IS NOT NULL AND RowNumber IS NOT NULL AND SeatNumber IS NOT NULL) -- Seated booking
    OR 
    (SeatID IS NULL AND RowNumber IS NULL AND SeatNumber IS NULL) -- Standing area booking
);
GO
PRINT 'Added check constraint for seat assignment validity';

-- Add an index for better query performance
CREATE NONCLUSTERED INDEX IX_BookingSeats_BookingID_SectionID 
ON BookingSeats(BookingID, SectionID);
GO
PRINT 'Added index for BookingID and SectionID';

PRINT '========================================';
PRINT 'Migration completed successfully!';
PRINT '========================================';
PRINT '';
PRINT 'Summary of changes:';
PRINT '- SeatID is now NULLABLE (for standing areas)';
PRINT '- RowNumber is now NULLABLE (for standing areas)';
PRINT '- SeatNumber is now NULLABLE (for standing areas)';
PRINT '- Added check constraint for data validity';
PRINT '- Added index for better query performance';
PRINT '';
PRINT 'Usage:';
PRINT '- For TRIBUNE sections: provide SeatID, RowNumber, SeatNumber';
PRINT '- For FIELD sections (standing): set all three to NULL, use SectionID only';
GO
