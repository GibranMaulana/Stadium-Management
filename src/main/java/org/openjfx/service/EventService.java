package org.openjfx.service;

import org.openjfx.model.Event;
import org.openjfx.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    // Get all events
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, " +
                    "ISNULL((SELECT SUM(b.TotalSeats) FROM Bookings b " +
                    "WHERE b.EventID = e.EventID AND b.BookingStatus = 'CONFIRMED'), 0) AS ActualBookedSeats " +
                    "FROM Events e " +
                    "ORDER BY e.EventDate DESC, e.EventTime DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all events: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    // Get event by ID
    public Event getEventById(int id) {
        String sql = "SELECT e.*, " +
                    "ISNULL((SELECT SUM(b.TotalSeats) FROM Bookings b " +
                    "WHERE b.EventID = e.EventID AND b.BookingStatus = 'CONFIRMED'), 0) AS ActualBookedSeats " +
                    "FROM Events e " +
                    "WHERE e.EventID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEvent(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting event by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Create new event
    public boolean createEvent(Event event) {
        String sql = "INSERT INTO Events (EventName, EventType, EventDate, EventTime, Description, Status, TotalSeats, BookedSeats) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, event.getEventName());
            pstmt.setString(2, event.getEventType());
            pstmt.setDate(3, Date.valueOf(event.getEventDate()));
            pstmt.setTime(4, Time.valueOf(event.getEventTime()));
            pstmt.setString(5, event.getDescription());
            pstmt.setString(6, event.getStatus());
            pstmt.setInt(7, event.getTotalSeats());
            pstmt.setInt(8, event.getBookedSeats());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Get generated ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getInt(1));
                }
                System.out.println("Event created successfully: " + event.getEventName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Update event
    public boolean updateEvent(Event event) {
        String sql = "UPDATE Events SET EventName = ?, EventType = ?, EventDate = ?, EventTime = ?, " +
                     "Description = ?, Status = ?, TotalSeats = ?, UpdatedAt = GETDATE() WHERE EventID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getEventName());
            pstmt.setString(2, event.getEventType());
            pstmt.setDate(3, Date.valueOf(event.getEventDate()));
            pstmt.setTime(4, Time.valueOf(event.getEventTime()));
            pstmt.setString(5, event.getDescription());
            pstmt.setString(6, event.getStatus());
            pstmt.setInt(7, event.getTotalSeats());
            pstmt.setInt(8, event.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Event updated successfully: " + event.getEventName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Delete event
    public boolean deleteEvent(int eventId) {
        // First check if there are bookings for this event
        if (hasBookings(eventId)) {
            System.err.println("Cannot delete event with existing bookings");
            return false;
        }

        // Delete in correct order: EventSections first, then Event
        String deleteEventSectionsSql = "DELETE FROM EventSections WHERE EventID = ?";
        String deleteEventSql = "DELETE FROM Events WHERE EventID = ?";

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Delete EventSections first
                try (PreparedStatement pstmt = conn.prepareStatement(deleteEventSectionsSql)) {
                    pstmt.setInt(1, eventId);
                    int sectionsDeleted = pstmt.executeUpdate();
                    System.out.println("Deleted " + sectionsDeleted + " event sections");
                }
                
                // Then delete the Event
                try (PreparedStatement pstmt = conn.prepareStatement(deleteEventSql)) {
                    pstmt.setInt(1, eventId);
                    int affectedRows = pstmt.executeUpdate();
                    
                    if (affectedRows > 0) {
                        conn.commit();
                        System.out.println("Event deleted successfully");
                        return true;
                    }
                }
                
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Search events by name or type
    public List<Event> searchEvents(String searchTerm) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM Events WHERE EventName LIKE ? OR EventType LIKE ? " +
                     "ORDER BY EventDate DESC, EventTime DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching events: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    // Filter events by type
    public List<Event> filterEventsByType(String eventType) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM Events WHERE EventType = ? ORDER BY EventDate DESC, EventTime DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, eventType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error filtering events: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    // Filter events by status
    public List<Event> filterEventsByStatus(String status) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM Events WHERE Status = ? ORDER BY EventDate DESC, EventTime DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error filtering events by status: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    // Get upcoming events
    public List<Event> getUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM Events WHERE EventDate >= CAST(GETDATE() AS DATE) AND Status = 'UPCOMING' " +
                     "ORDER BY EventDate ASC, EventTime ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting upcoming events: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    // Get event count
    public int getEventCount() {
        String sql = "SELECT COUNT(*) FROM Events";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting event count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // Check if event has bookings
    public boolean hasBookings(int eventId) {
        String sql = "SELECT COUNT(*) FROM Bookings WHERE EventID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking bookings: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Update booked seats count
    public boolean updateBookedSeats(int eventId, int bookedSeats) {
        String sql = "UPDATE Events SET BookedSeats = ?, UpdatedAt = GETDATE() WHERE EventID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookedSeats);
            pstmt.setInt(2, eventId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating booked seats: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Get available event types
    public List<String> getEventTypes() {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT EventType FROM Events ORDER BY EventType";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                types.add(rs.getString("EventType"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting event types: " + e.getMessage());
            e.printStackTrace();
        }

        return types;
    }

    // Helper method to map ResultSet to Event object
    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        int id = rs.getInt("EventID");
        String eventName = rs.getString("EventName");
        String eventType = rs.getString("EventType");
        LocalDate eventDate = rs.getDate("EventDate").toLocalDate();
        LocalTime eventTime = rs.getTime("EventTime").toLocalTime();
        String description = rs.getString("Description");
        String status = rs.getString("Status");
        int totalSeats = rs.getInt("TotalSeats");
        
        // Try to get ActualBookedSeats (from JOIN query), fallback to BookedSeats
        int bookedSeats;
        try {
            bookedSeats = rs.getInt("ActualBookedSeats");
        } catch (SQLException e) {
            // If ActualBookedSeats doesn't exist (old queries), use BookedSeats
            bookedSeats = rs.getInt("BookedSeats");
        }
        
        Timestamp createdAt = rs.getTimestamp("CreatedAt");
        Timestamp updatedAt = rs.getTimestamp("UpdatedAt");

        return new Event(id, eventName, eventType, eventDate, eventTime, description,
                        status, totalSeats, bookedSeats, createdAt, updatedAt);
    }
}
