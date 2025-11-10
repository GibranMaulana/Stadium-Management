package org.openjfx.model;

import java.sql.Timestamp;

/**
 * Model class representing a link between booking and seat
 * For field/standing tickets, seatId, rowNumber, seatNumber will be null
 */
public class BookingSeat {
    private int bookingSeatId;
    private int bookingId;
    private int eventId;
    private int sectionId;
    private Integer seatId; // Nullable for field/standing tickets
    private String rowNumber; // Nullable for field/standing tickets
    private Integer seatNumber; // Nullable for field/standing tickets
    private double price;
    private String status; // ACTIVE, CANCELLED
    private Timestamp createdAt;
    
    // Additional fields for display
    private String sectionName;
    private String sectionTitle;
    
    // Constructors
    public BookingSeat() {}
    
    public BookingSeat(int bookingSeatId, int bookingId, int eventId, int sectionId,
                      Integer seatId, String rowNumber, Integer seatNumber,
                      double price, String status, Timestamp createdAt) {
        this.bookingSeatId = bookingSeatId;
        this.bookingId = bookingId;
        this.eventId = eventId;
        this.sectionId = sectionId;
        this.seatId = seatId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getBookingSeatId() {
        return bookingSeatId;
    }
    
    public void setBookingSeatId(int bookingSeatId) {
        this.bookingSeatId = bookingSeatId;
    }
    
    public int getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    
    public int getEventId() {
        return eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    
    public int getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
    
    public Integer getSeatId() {
        return seatId;
    }
    
    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }
    
    public String getRowNumber() {
        return rowNumber;
    }
    
    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }
    
    public Integer getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getSectionName() {
        return sectionName;
    }
    
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
    
    public String getSectionTitle() {
        return sectionTitle;
    }
    
    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }
    
    // Helper methods
    public String getSeatLabel() {
        if (isStandingTicket()) {
            return "Standing";
        }
        return rowNumber + "-" + seatNumber;
    }
    
    public boolean isStandingTicket() {
        return seatId == null || rowNumber == null || seatNumber == null;
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    @Override
    public String toString() {
        return getSeatLabel() + " - Rp " + String.format("%.2f", price);
    }
}
