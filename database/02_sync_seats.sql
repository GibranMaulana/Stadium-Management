USE StadiumDB;
GO

PRINT '========================================';
PRINT 'SEAT GENERATION SCRIPT';
PRINT '========================================';
PRINT 'This script will generate seat records for all tribune sections';
PRINT 'based on their configured dimensions (TotalRows × SeatsPerRow)';
PRINT '';

-- Check if seats already exist
DECLARE @ExistingSeats INT;
SELECT @ExistingSeats = COUNT(*) FROM Seats;

IF @ExistingSeats > 0
BEGIN
    PRINT 'WARNING: ' + CAST(@ExistingSeats AS NVARCHAR(10)) + ' seats already exist!';
    PRINT 'This script will DELETE all existing seats and regenerate them.';
    PRINT '';
    PRINT 'Press Ctrl+C to cancel, or continue to proceed...';
    PRINT '';
    
    -- Delete all existing seats
    DELETE FROM Seats;
    PRINT 'Deleted ' + CAST(@ExistingSeats AS NVARCHAR(10)) + ' existing seats';
    PRINT '';
END

-- =============================================
-- GENERATE SEATS FOR ALL TRIBUNE SECTIONS
-- =============================================

DECLARE @SectionID INT;
DECLARE @SectionName NVARCHAR(100);
DECLARE @TotalRows INT;
DECLARE @SeatsPerRow INT;
DECLARE @RowNum INT;
DECLARE @SeatNum INT;
DECLARE @RowLabel NVARCHAR(10);
DECLARE @TotalSeatsCreated INT = 0;

-- Cursor for tribune sections
DECLARE section_cursor CURSOR FOR
SELECT SectionID, SectionName, TotalRows, SeatsPerRow
FROM Sections
WHERE SectionType = 'TRIBUNE' AND IsActive = 1;

OPEN section_cursor;
FETCH NEXT FROM section_cursor INTO @SectionID, @SectionName, @TotalRows, @SeatsPerRow;

WHILE @@FETCH_STATUS = 0
BEGIN
    PRINT 'Generating seats for: ' + @SectionName;
    PRINT '  Configuration: ' + CAST(@TotalRows AS NVARCHAR(10)) + ' rows × ' + 
          CAST(@SeatsPerRow AS NVARCHAR(10)) + ' seats = ' + 
          CAST(@TotalRows * @SeatsPerRow AS NVARCHAR(10)) + ' total seats';
    
    SET @RowNum = 1;
    
    -- Generate seats row by row
    WHILE @RowNum <= @TotalRows
    BEGIN
        -- Generate row label (A-Z, then AA-AZ, BA-BZ, etc.)
        IF @RowNum <= 26
            SET @RowLabel = CHAR(64 + @RowNum); -- A, B, C, ..., Z
        ELSE
        BEGIN
            DECLARE @FirstChar INT = (@RowNum - 27) / 26;
            DECLARE @SecondChar INT = (@RowNum - 27) % 26;
            SET @RowLabel = CHAR(65 + @FirstChar) + CHAR(65 + @SecondChar); -- AA, AB, AC, ...
        END
        
        SET @SeatNum = 1;
        
        -- Generate all seats in this row
        WHILE @SeatNum <= @SeatsPerRow
        BEGIN
            INSERT INTO Seats (SectionID, RowNumber, SeatNumber, IsActive)
            VALUES (@SectionID, @RowLabel, @SeatNum, 1);
            
            SET @SeatNum = @SeatNum + 1;
            SET @TotalSeatsCreated = @TotalSeatsCreated + 1;
        END
        
        SET @RowNum = @RowNum + 1;
    END
    
    PRINT '  ✓ Created ' + CAST(@TotalRows * @SeatsPerRow AS NVARCHAR(10)) + ' seats';
    PRINT '';
    
    FETCH NEXT FROM section_cursor INTO @SectionID, @SectionName, @TotalRows, @SeatsPerRow;
END

CLOSE section_cursor;
DEALLOCATE section_cursor;

PRINT '========================================';
PRINT 'SEAT GENERATION COMPLETE!';
PRINT '========================================';
PRINT 'Total seats created: ' + CAST(@TotalSeatsCreated AS NVARCHAR(10));
PRINT '';

-- Display summary
SELECT 
    s.SectionName,
    s.TotalRows,
    s.SeatsPerRow,
    s.TotalRows * s.SeatsPerRow AS ConfiguredCapacity,
    COUNT(se.SeatID) AS ActualSeats,
    CASE 
        WHEN COUNT(se.SeatID) = s.TotalRows * s.SeatsPerRow THEN '✓ MATCH'
        ELSE '✗ MISMATCH'
    END AS Status
FROM Sections s
LEFT JOIN Seats se ON s.SectionID = se.SectionID
WHERE s.SectionType = 'TRIBUNE'
GROUP BY s.SectionID, s.SectionName, s.TotalRows, s.SeatsPerRow
ORDER BY s.SectionID;

GO
