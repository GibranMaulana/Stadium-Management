package org.openjfx.service;

import org.openjfx.model.EventSection;
import org.openjfx.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for EventSection-related database operations
 */
public class EventSectionService {
    
    /**
     * Get all event sections for a specific event
     */
    public List<EventSection> getEventSections(int eventId) {
        List<EventSection> eventSections = new ArrayList<>();
        String query = "SELECT es.*, s.SectionName, s.SectionType " +
                      "FROM EventSections es " +
                      "INNER JOIN Sections s ON es.SectionID = s.SectionID " +
                      "WHERE es.EventID = ? AND es.IsActive = 1 " +
                      "ORDER BY es.SectionID";
        
        System.out.println("DEBUG EventSectionService: Fetching sections for EventID=" + eventId);
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EventSection eventSection = mapResultSetToEventSection(rs);
                    eventSection.setSectionName(rs.getString("SectionName"));
                    eventSection.setSectionType(rs.getString("SectionType"));
                    
                    System.out.println("DEBUG: Loaded EventSection - ID=" + eventSection.getEventSectionId() + 
                                     ", Title=" + eventSection.getSectionTitle() + 
                                     ", TotalCapacity=" + eventSection.getTotalCapacity() + 
                                     ", AvailableSeats=" + eventSection.getAvailableSeats());
                    
                    eventSections.add(eventSection);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching event sections: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("DEBUG EventSectionService: Found " + eventSections.size() + " sections");
        return eventSections;
    }
    
    /**
     * Get a specific event section
     */
    public EventSection getEventSection(int eventId, int sectionId) {
        String query = "SELECT es.*, s.SectionName, s.SectionType " +
                      "FROM EventSections es " +
                      "INNER JOIN Sections s ON es.SectionID = s.SectionID " +
                      "WHERE es.EventID = ? AND es.SectionID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventId);
            stmt.setInt(2, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EventSection eventSection = mapResultSetToEventSection(rs);
                    eventSection.setSectionName(rs.getString("SectionName"));
                    eventSection.setSectionType(rs.getString("SectionType"));
                    return eventSection;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching event section: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Create event sections for an event
     */
    public boolean createEventSection(EventSection eventSection) {
        String query = "INSERT INTO EventSections " +
                      "(EventID, SectionID, SectionTitle, Price, TotalCapacity, AvailableCapacity) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventSection.getEventId());
            stmt.setInt(2, eventSection.getSectionId());
            stmt.setString(3, eventSection.getSectionTitle());
            stmt.setDouble(4, eventSection.getPrice());
            stmt.setInt(5, eventSection.getTotalCapacity());
            stmt.setInt(6, eventSection.getAvailableSeats());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating event section: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update available capacity for an event section
     */
    public boolean updateAvailableCapacity(int eventSectionId, int newCapacity) {
        String query = "UPDATE EventSections SET AvailableCapacity = ?, UpdatedAt = GETDATE() " +
                      "WHERE EventSectionID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, newCapacity);
            stmt.setInt(2, eventSectionId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating capacity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Decrease available capacity (when booking)
     */
    public boolean decreaseCapacity(int eventId, int sectionId, int amount) {
        String query = "UPDATE EventSections " +
                      "SET AvailableCapacity = AvailableCapacity - ?, UpdatedAt = GETDATE() " +
                      "WHERE EventID = ? AND SectionID = ? AND AvailableCapacity >= ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, amount);
            stmt.setInt(2, eventId);
            stmt.setInt(3, sectionId);
            stmt.setInt(4, amount);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error decreasing capacity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Increase available capacity (when cancelling booking)
     */
    public boolean increaseCapacity(int eventId, int sectionId, int amount) {
        String query = "UPDATE EventSections " +
                      "SET AvailableCapacity = AvailableCapacity + ?, UpdatedAt = GETDATE() " +
                      "WHERE EventID = ? AND SectionID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, amount);
            stmt.setInt(2, eventId);
            stmt.setInt(3, sectionId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error increasing capacity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if event section exists for an event
     */
    public boolean eventSectionExists(int eventId, int sectionId) {
        String query = "SELECT COUNT(*) FROM EventSections WHERE EventID = ? AND SectionID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventId);
            stmt.setInt(2, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking event section: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete event section configuration
     */
    public boolean deleteEventSection(int eventSectionId) {
        String query = "DELETE FROM EventSections WHERE EventSectionID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventSectionId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting event section: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Map ResultSet to EventSection object
     */
    private EventSection mapResultSetToEventSection(ResultSet rs) throws SQLException {
        return new EventSection(
            rs.getInt("EventSectionID"),
            rs.getInt("EventID"),
            rs.getInt("SectionID"),
            rs.getString("SectionTitle"),
            rs.getDouble("Price"),
            rs.getInt("TotalCapacity"),
            rs.getInt("AvailableCapacity"),
            rs.getTimestamp("CreatedAt"),
            rs.getTimestamp("UpdatedAt")
        );
    }
}
