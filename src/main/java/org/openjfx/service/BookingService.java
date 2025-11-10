package org.openjfx.service;

import org.openjfx.model.Booking;
import org.openjfx.model.BookingSeat;
import org.openjfx.model.Seat;
import org.openjfx.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Booking-related database operations
 */
public class BookingService {
    
    /**
     * Create a new booking with seats
     */
    public Booking createBooking(Booking booking, List<Seat> selectedSeats) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Generate booking number
            String bookingNumber = generateBookingNumber();
            booking.setBookingNumber(bookingNumber);
            
            // Insert booking
            String bookingQuery = "INSERT INTO Bookings " +
                                "(EventID, BookingNumber, CustomerName, CustomerEmail, CustomerPhone, " +
                                "TotalSeats, TotalPrice, BookingStatus) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            int bookingId;
            try (PreparedStatement stmt = conn.prepareStatement(bookingQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, booking.getEventId());
                stmt.setString(2, bookingNumber);
                stmt.setString(3, booking.getCustomerName());
                stmt.setString(4, booking.getCustomerEmail());
                stmt.setString(5, booking.getCustomerPhone());
                stmt.setInt(6, booking.getTotalSeats());
                stmt.setDouble(7, booking.getTotalPrice());
                stmt.setString(8, booking.getBookingStatus());
                
                stmt.executeUpdate();
                
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    bookingId = rs.getInt(1);
                    booking.setBookingId(bookingId);
                } else {
                    throw new SQLException("Failed to get booking ID");
                }
            }
            
            // Insert booking seats
            String seatQuery = "INSERT INTO BookingSeats " +
                             "(BookingID, EventID, SectionID, SeatID, RowNumber, SeatNumber, Price) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(seatQuery)) {
                for (Seat seat : selectedSeats) {
                    stmt.setInt(1, bookingId);
                    stmt.setInt(2, booking.getEventId());
                    stmt.setInt(3, seat.getSectionId());
                    
                    if (seat.getSeatId() > 0) {
                        // Tribune seat
                        stmt.setInt(4, seat.getSeatId());
                        stmt.setString(5, seat.getRowNumber());
                        stmt.setInt(6, seat.getSeatNumber());
                    } else {
                        // Field/standing ticket
                        stmt.setNull(4, Types.INTEGER);
                        stmt.setNull(5, Types.VARCHAR);
                        stmt.setNull(6, Types.INTEGER);
                    }
                    
                    // Price per seat (assume equal distribution)
                    double pricePerSeat = booking.getTotalPrice() / selectedSeats.size();
                    stmt.setDouble(7, pricePerSeat);
                    
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            // Update event section capacity IN THE SAME TRANSACTION
            if (selectedSeats.size() > 0) {
                int sectionId = selectedSeats.get(0).getSectionId();
                String capacityQuery = "UPDATE EventSections " +
                                     "SET AvailableCapacity = AvailableCapacity - ?, UpdatedAt = GETDATE() " +
                                     "WHERE EventID = ? AND SectionID = ? AND AvailableCapacity >= ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(capacityQuery)) {
                    stmt.setInt(1, selectedSeats.size());
                    stmt.setInt(2, booking.getEventId());
                    stmt.setInt(3, sectionId);
                    stmt.setInt(4, selectedSeats.size());
                    
                    int updated = stmt.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Failed to update capacity - not enough seats available");
                    }
                }
            }
            
            conn.commit(); // Commit transaction
            return booking;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            return null;
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
     * Get all bookings for an event
     */
    public List<Booking> getBookingsByEvent(int eventId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, e.EventName FROM Bookings b " +
                      "INNER JOIN Events e ON b.EventID = e.EventID " +
                      "WHERE b.EventID = ? " +
                      "ORDER BY b.BookingDate DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, eventId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Booking booking = mapResultSetToBooking(rs);
                    booking.setEventName(rs.getString("EventName"));
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return bookings;
    }
    
    /**
     * Get all bookings
     */
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, e.EventName FROM Bookings b " +
                      "INNER JOIN Events e ON b.EventID = e.EventID " +
                      "ORDER BY b.BookingDate DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Booking booking = mapResultSetToBooking(rs);
                booking.setEventName(rs.getString("EventName"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all bookings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return bookings;
    }
    
    /**
     * Get booking by ID with seats
     */
    public Booking getBookingById(int bookingId) {
        String query = "SELECT b.*, e.EventName FROM Bookings b " +
                      "INNER JOIN Events e ON b.EventID = e.EventID " +
                      "WHERE b.BookingID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, bookingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = mapResultSetToBooking(rs);
                    booking.setEventName(rs.getString("EventName"));
                    
                    // Load booking seats
                    booking.setBookingSeats(getBookingSeats(bookingId));
                    
                    return booking;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get booking seats for a booking
     */
    public List<BookingSeat> getBookingSeats(int bookingId) {
        List<BookingSeat> bookingSeats = new ArrayList<>();
        String query = "SELECT bs.*, s.SectionName, es.SectionTitle " +
                      "FROM BookingSeats bs " +
                      "INNER JOIN Sections s ON bs.SectionID = s.SectionID " +
                      "LEFT JOIN EventSections es ON bs.EventID = es.EventID AND bs.SectionID = es.SectionID " +
                      "WHERE bs.BookingID = ? " +
                      "ORDER BY bs.RowNumber, bs.SeatNumber";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, bookingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookingSeat bookingSeat = mapResultSetToBookingSeat(rs);
                    bookingSeat.setSectionName(rs.getString("SectionName"));
                    bookingSeat.setSectionTitle(rs.getString("SectionTitle"));
                    bookingSeats.add(bookingSeat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking seats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return bookingSeats;
    }
    
    /**
     * Cancel a booking
     */
    public boolean cancelBooking(int bookingId) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Get booking details
            Booking booking = getBookingById(bookingId);
            if (booking == null) {
                return false;
            }
            
            // Update booking status
            String bookingQuery = "UPDATE Bookings SET BookingStatus = 'CANCELLED', UpdatedAt = GETDATE() " +
                                "WHERE BookingID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(bookingQuery)) {
                stmt.setInt(1, bookingId);
                stmt.executeUpdate();
            }
            
            // Update booking seats status
            String seatsQuery = "UPDATE BookingSeats SET Status = 'CANCELLED' WHERE BookingID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(seatsQuery)) {
                stmt.setInt(1, bookingId);
                stmt.executeUpdate();
            }
            
            // Increase event section capacity back
            if (booking.getBookingSeats().size() > 0) {
                int sectionId = booking.getBookingSeats().get(0).getSectionId();
                EventSectionService eventSectionService = new EventSectionService();
                eventSectionService.increaseCapacity(booking.getEventId(), sectionId, booking.getTotalSeats());
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
            return false;
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
     * Delete a booking permanently
     * This will remove the booking and all related data from the database
     */
    public boolean deleteBooking(int bookingId) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Get booking details before deletion
            Booking booking = getBookingById(bookingId);
            if (booking == null) {
                System.err.println("Booking not found: " + bookingId);
                return false;
            }
            
            // Delete in correct order: BookingSeats first, then Booking
            
            // 1. Delete BookingSeats
            String deleteBookingSeatsSql = "DELETE FROM BookingSeats WHERE BookingID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteBookingSeatsSql)) {
                stmt.setInt(1, bookingId);
                int seatsDeleted = stmt.executeUpdate();
                System.out.println("Deleted " + seatsDeleted + " booking seats");
            }
            
            // 2. Delete Booking
            String deleteBookingSql = "DELETE FROM Bookings WHERE BookingID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteBookingSql)) {
                stmt.setInt(1, bookingId);
                int deleted = stmt.executeUpdate();
                
                if (deleted == 0) {
                    System.err.println("Failed to delete booking");
                    conn.rollback();
                    return false;
                }
            }
            
            // 3. Restore event section capacity if booking was CONFIRMED
            if ("CONFIRMED".equals(booking.getBookingStatus()) && booking.getBookingSeats().size() > 0) {
                int sectionId = booking.getBookingSeats().get(0).getSectionId();
                EventSectionService eventSectionService = new EventSectionService();
                eventSectionService.increaseCapacity(booking.getEventId(), sectionId, booking.getTotalSeats());
                System.out.println("Restored capacity: " + booking.getTotalSeats() + " seats");
            }
            
            conn.commit();
            System.out.println("Booking deleted successfully: " + booking.getBookingNumber());
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error deleting booking: " + e.getMessage());
            e.printStackTrace();
            return false;
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
     * Get today's bookings count
     */
    public int getTodayBookingsCount() {
        String query = "SELECT COUNT(*) FROM Bookings " +
                      "WHERE CAST(BookingDate AS DATE) = CAST(GETDATE() AS DATE)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's bookings count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get total revenue (all confirmed bookings)
     */
    public double getTotalRevenue() {
        String query = "SELECT COALESCE(SUM(TotalPrice), 0) FROM Bookings " +
                      "WHERE BookingStatus = 'CONFIRMED'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Generate unique booking number
     */
    private String generateBookingNumber() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Get count of bookings today
        String query = "SELECT COUNT(*) FROM Bookings WHERE BookingNumber LIKE 'BK-" + dateStr + "-%'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            return String.format("BK-%s-%04d", dateStr, count + 1);
            
        } catch (SQLException e) {
            System.err.println("Error generating booking number: " + e.getMessage());
            e.printStackTrace();
            return "BK-" + dateStr + "-" + System.currentTimeMillis();
        }
    }
    
    /**
     * Map ResultSet to Booking object
     */
    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        return new Booking(
            rs.getInt("BookingID"),
            rs.getInt("EventID"),
            rs.getString("BookingNumber"),
            rs.getString("CustomerName"),
            rs.getString("CustomerEmail"),
            rs.getString("CustomerPhone"),
            rs.getInt("TotalSeats"),
            rs.getDouble("TotalPrice"),
            rs.getString("BookingStatus"),
            rs.getTimestamp("BookingDate"),
            rs.getTimestamp("CreatedAt"),
            rs.getTimestamp("UpdatedAt")
        );
    }
    
    /**
     * Map ResultSet to BookingSeat object
     */
    private BookingSeat mapResultSetToBookingSeat(ResultSet rs) throws SQLException {
        Integer seatId = rs.getObject("SeatID", Integer.class);
        String rowNumber = rs.getString("RowNumber");
        Integer seatNumber = rs.getObject("SeatNumber", Integer.class);
        
        return new BookingSeat(
            rs.getInt("BookingSeatID"),
            rs.getInt("BookingID"),
            rs.getInt("EventID"),
            rs.getInt("SectionID"),
            seatId,
            rowNumber,
            seatNumber,
            rs.getDouble("Price"),
            rs.getString("Status"),
            rs.getTimestamp("CreatedAt")
        );
    }
}
