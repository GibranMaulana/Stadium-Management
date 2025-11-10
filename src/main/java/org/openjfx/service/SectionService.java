package org.openjfx.service;

import org.openjfx.model.Section;
import org.openjfx.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Section-related database operations
 */
public class SectionService {
    
    /**
     * Get all sections from the database
     */
    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList<>();
        String query = "SELECT * FROM Sections WHERE IsActive = 1 ORDER BY SectionID";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sections: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sections;
    }
    
    /**
     * Get section by ID
     */
    public Section getSectionById(int sectionId) {
        String query = "SELECT * FROM Sections WHERE SectionID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSection(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching section: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get tribune sections only (with seats)
     */
    public List<Section> getTribuneSections() {
        List<Section> sections = new ArrayList<>();
        String query = "SELECT * FROM Sections WHERE SectionType = 'TRIBUNE' AND IsActive = 1 ORDER BY SectionID";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tribune sections: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sections;
    }
    
    /**
     * Get field sections only (standing areas)
     */
    public List<Section> getFieldSections() {
        List<Section> sections = new ArrayList<>();
        String query = "SELECT * FROM Sections WHERE SectionType = 'FIELD' AND IsActive = 1 ORDER BY SectionID";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching field sections: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sections;
    }
    
    /**
     * Update section details
     */
    public boolean updateSection(Section section) {
        String query = "UPDATE Sections SET SectionName = ?, TotalRows = ?, SeatsPerRow = ?, " +
                      "Description = ?, UpdatedAt = GETDATE() WHERE SectionID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, section.getSectionName());
            stmt.setInt(2, section.getTotalRows());
            stmt.setInt(3, section.getSeatsPerRow());
            stmt.setString(4, ""); // Description - can add to Section model later
            stmt.setInt(5, section.getSectionId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating section: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get total stadium capacity based on event type
     * For Football: Only tribune sections
     * For Concert/Other: All sections (tribune + field)
     */
    public int getTotalCapacityForEventType(String eventType) {
        List<Section> sections;
        if (eventType != null && eventType.toUpperCase().contains("FOOTBALL")) {
            sections = getTribuneSections();
        } else {
            sections = getAllSections();
        }
        
        int total = 0;
        for (Section section : sections) {
            total += section.getTotalCapacity();
        }
        return total;
    }
    
    /**
     * Map ResultSet to Section object
     */
    private Section mapResultSetToSection(ResultSet rs) throws SQLException {
        int totalRows = rs.getInt("TotalRows");
        int seatsPerRow = rs.getInt("SeatsPerRow");
        int totalCapacity = totalRows * seatsPerRow; // Calculate capacity
        
        return new Section(
            rs.getInt("SectionID"),
            rs.getString("SectionName"),
            rs.getString("SectionType"),
            totalRows,
            seatsPerRow,
            totalCapacity
        );
    }
}
