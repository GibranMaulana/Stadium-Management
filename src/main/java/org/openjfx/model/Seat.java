package org.openjfx.model;

/**
 * Model class representing an individual seat in a tribune section
 */
public class Seat {
    private int seatId;
    private int sectionId;
    private String rowNumber;
    private int seatNumber;
    private String status; // AVAILABLE or BOOKED
    
    // Constructors
    public Seat() {}
    
    public Seat(int seatId, int sectionId, String rowNumber, int seatNumber, String status) {
        this.seatId = seatId;
        this.sectionId = sectionId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.status = status;
    }
    
    // Getters and Setters
    public int getSeatId() {
        return seatId;
    }
    
    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }
    
    public int getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
    
    public String getRowNumber() {
        return rowNumber;
    }
    
    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }
    
    public int getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    // Helper methods
    public String getSeatLabel() {
        return rowNumber + "-" + seatNumber;
    }
    
    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }
    
    @Override
    public String toString() {
        return getSeatLabel();
    }
}
