package org.openjfx.service;

import org.openjfx.model.Seat;
import org.openjfx.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Seat-related database operations
 */
public class SeatService {
    
    /**
     * Get all seats for a specific section
     */
    public List<Seat> getSeatsBySection(int sectionId) {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT * FROM Seats WHERE SectionID = ? AND IsActive = 1 ORDER BY RowNumber, SeatNumber";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapResultSetToSeat(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return seats;
    }
    
    /**
     * Get available seats for an event and section
     */
    public List<Seat> getAvailableSeats(int eventId, int sectionId) {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT s.* FROM Seats s " +
                      "WHERE s.SectionID = ? AND s.IsActive = 1 " +
                      "AND s.SeatID NOT IN (" +
                      "    SELECT bs.SeatID FROM BookingSeats bs " +
                      "    INNER JOIN Bookings b ON bs.BookingID = b.BookingID " +
                      "    WHERE bs.EventID = ? AND bs.SeatID IS NOT NULL " +
                      "    AND bs.Status = 'BOOKED' AND b.BookingStatus != 'CANCELLED'" +
                      ") " +
                      "ORDER BY s.RowNumber, s.SeatNumber";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, sectionId);
            stmt.setInt(2, eventId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = mapResultSetToSeat(rs);
                    seat.setStatus("AVAILABLE");
                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available seats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return seats;
    }
    
    /**
     * Get booked seats for an event and section
     */
    public List<Seat> getBookedSeats(int eventId, int sectionId) {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT s.* FROM Seats s " +
                      "INNER JOIN BookingSeats bs ON s.SeatID = bs.SeatID " +
                      "INNER JOIN Bookings b ON bs.BookingID = b.BookingID " +
                      "WHERE bs.EventID = ? AND s.SectionID = ? " +
                      "AND bs.Status = 'BOOKED' AND b.BookingStatus != 'CANCELLED' " +
                      "ORDER BY s.RowNumber, s.SeatNumber";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventId);
            stmt.setInt(2, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = mapResultSetToSeat(rs);
                    seat.setStatus("BOOKED");
                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booked seats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return seats;
    }
    
    /**
     * Get seat by ID
     */
    public Seat getSeatById(int seatId) {
        String query = "SELECT * FROM Seats WHERE SeatID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, seatId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSeat(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seat: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Check if a seat is available for booking
     */
    public boolean isSeatAvailable(int eventId, int seatId) {
        String query = "SELECT COUNT(*) FROM BookingSeats bs " +
                      "INNER JOIN Bookings b ON bs.BookingID = b.BookingID " +
                      "WHERE bs.EventID = ? AND bs.SeatID = ? " +
                      "AND bs.Status = 'BOOKED' AND b.BookingStatus != 'CANCELLED'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventId);
            stmt.setInt(2, seatId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Available if count is 0
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking seat availability: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get total seats count across all sections
     */
    public int getTotalSeatsCount() {
        String query = "SELECT COUNT(*) FROM Seats WHERE IsActive = 1";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total seats count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get available seats count for an event
     */
    public int getAvailableSeatsCount(int eventId) {
        String query = "SELECT COUNT(*) FROM Seats s " +
                      "WHERE s.IsActive = 1 " +
                      "AND s.SeatID NOT IN (" +
                      "    SELECT bs.SeatID FROM BookingSeats bs " +
                      "    INNER JOIN Bookings b ON bs.BookingID = b.BookingID " +
                      "    WHERE bs.EventID = ? AND bs.SeatID IS NOT NULL " +
                      "    AND bs.Status = 'BOOKED' AND b.BookingStatus != 'CANCELLED'" +
                      ")";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting available seats count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Map ResultSet to Seat object
     */
    private Seat mapResultSetToSeat(ResultSet rs) throws SQLException {
        return new Seat(
            rs.getInt("SeatID"),
            rs.getInt("SectionID"),
            rs.getString("RowNumber"),
            rs.getInt("SeatNumber"),
            "AVAILABLE" // Default status
        );
    }
}
