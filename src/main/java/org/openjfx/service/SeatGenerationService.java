package org.openjfx.service;

import org.openjfx.model.Section;
import org.openjfx.util.DatabaseUtil;

import java.sql.*;

/**
 * Service for automatically generating and managing seat records
 * based on section configuration
 */
public class SeatGenerationService {
    
    /**
     * Regenerates all seats for a section based on its current configuration
     * Deletes existing seats and creates new ones according to TotalRows and SeatsPerRow
     * 
     * @param sectionId The section to regenerate seats for
     * @throws SQLException If database operation fails
     */
    public void regenerateSeatsForSection(int sectionId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Use transaction
            
            // Get section details
            Section section = getSectionById(sectionId, conn);
            if (section == null) {
                throw new SQLException("Section not found: " + sectionId);
            }
            
            // Only generate seats for TRIBUNE sections (seated)
            if (!"TRIBUNE".equals(section.getSectionType())) {
                System.out.println("Skipping seat generation for non-TRIBUNE section: " + section.getSectionName());
                return;
            }
            
            System.out.println("Regenerating seats for section: " + section.getSectionName() + 
                             " (" + section.getTotalRows() + " rows × " + 
                             section.getSeatsPerRow() + " seats = " + 
                             section.getTotalCapacity() + " total)");
            
            // Delete existing seats for this section
            deleteSeatsForSection(sectionId, conn);
            
            // Generate new seats
            generateSeatsForSection(section, conn);
            
            conn.commit();
            System.out.println("✓ Successfully regenerated " + section.getTotalCapacity() + 
                             " seats for " + section.getSectionName());
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("✗ Rolled back seat regeneration due to error");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Generates seats for all tribune sections in the database
     * Useful for initial setup or complete regeneration
     */
    public void regenerateAllSeats() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            String query = "SELECT SectionID FROM Sections WHERE SectionType = 'TRIBUNE'";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            int sectionsProcessed = 0;
            while (rs.next()) {
                int sectionId = rs.getInt("SectionID");
                try {
                    regenerateSeatsForSection(sectionId);
                    sectionsProcessed++;
                } catch (SQLException e) {
                    System.err.println("Failed to regenerate seats for section " + sectionId + ": " + e.getMessage());
                }
            }
            
            System.out.println("\n========================================");
            System.out.println("Seat regeneration complete!");
            System.out.println("Processed " + sectionsProcessed + " tribune sections");
            System.out.println("========================================\n");
            
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    /**
     * Deletes all seat records for a specific section
     */
    private void deleteSeatsForSection(int sectionId, Connection conn) throws SQLException {
        // First, check if any seats are booked
        String checkQuery = "SELECT COUNT(*) FROM BookingSeats bs " +
                          "INNER JOIN Seats s ON bs.SeatID = s.SeatID " +
                          "WHERE s.SectionID = ?";
        
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setInt(1, sectionId);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next() && rs.getInt(1) > 0) {
            throw new SQLException("Cannot regenerate seats: " + rs.getInt(1) + 
                                 " seats have existing bookings. Cancel bookings first.");
        }
        
        // Delete existing seats
        String deleteQuery = "DELETE FROM Seats WHERE SectionID = ?";
        PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
        deleteStmt.setInt(1, sectionId);
        int deleted = deleteStmt.executeUpdate();
        System.out.println("  - Deleted " + deleted + " existing seats");
    }
    
    /**
     * Generates seat records based on section configuration
     */
    private void generateSeatsForSection(Section section, Connection conn) throws SQLException {
        String insertQuery = "INSERT INTO Seats (SectionID, RowNumber, SeatNumber) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertQuery);
        
        int seatsCreated = 0;
        
        // Generate seats row by row
        for (int rowNum = 1; rowNum <= section.getTotalRows(); rowNum++) {
            String rowLabel = generateRowLabel(rowNum);
            
            for (int seatNum = 1; seatNum <= section.getSeatsPerRow(); seatNum++) {
                stmt.setInt(1, section.getSectionId());
                stmt.setString(2, rowLabel);
                stmt.setInt(3, seatNum);
                stmt.addBatch();
                seatsCreated++;
                
                // Execute batch every 500 inserts for performance
                if (seatsCreated % 500 == 0) {
                    stmt.executeBatch();
                    System.out.println("  - Created " + seatsCreated + " seats...");
                }
            }
        }
        
        // Execute remaining batch
        stmt.executeBatch();
        System.out.println("  - Created " + seatsCreated + " seats total");
    }
    
    /**
     * Generates row label (A-Z, then AA-AZ, BA-BZ, etc.)
     */
    private String generateRowLabel(int rowNum) {
        if (rowNum <= 26) {
            // A-Z
            return String.valueOf((char) ('A' + rowNum - 1));
        } else {
            // AA, AB, AC, ... (for rows beyond Z)
            int firstChar = (rowNum - 27) / 26;
            int secondChar = (rowNum - 27) % 26;
            return String.valueOf((char) ('A' + firstChar)) + (char) ('A' + secondChar);
        }
    }
    
    /**
     * Gets section details from database
     */
    private Section getSectionById(int sectionId, Connection conn) throws SQLException {
        String query = "SELECT * FROM Sections WHERE SectionID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, sectionId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Section section = new Section();
            section.setSectionId(rs.getInt("SectionID"));
            section.setSectionName(rs.getString("SectionName"));
            section.setSectionType(rs.getString("SectionType"));
            section.setTotalRows(rs.getInt("TotalRows"));
            section.setSeatsPerRow(rs.getInt("SeatsPerRow"));
            // Calculate capacity instead of reading from DB
            int totalCapacity = section.getTotalRows() * section.getSeatsPerRow();
            section.setTotalCapacity(totalCapacity);
            return section;
        }
        
        return null;
    }
    
    /**
     * Gets the current count of seats for a section
     */
    public int getSeatCountForSection(int sectionId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            String query = "SELECT COUNT(*) FROM Seats WHERE SectionID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
