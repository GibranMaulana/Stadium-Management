package org.openjfx.model;

import java.sql.Timestamp;

/**
 * Model class representing event-specific section configuration
 */
public class EventSection {
    private int eventSectionId;
    private int eventId;
    private int sectionId;
    private String sectionTitle; // Custom title for this event (e.g., "VIP", "Standard")
    private double price;
    private int totalCapacity;
    private int availableSeats;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display
    private String sectionName;
    private String sectionType;
    
    // Constructors
    public EventSection() {}
    
    public EventSection(int eventSectionId, int eventId, int sectionId, String sectionTitle,
                       double price, int totalCapacity, int availableSeats,
                       Timestamp createdAt, Timestamp updatedAt) {
        this.eventSectionId = eventSectionId;
        this.eventId = eventId;
        this.sectionId = sectionId;
        this.sectionTitle = sectionTitle;
        this.price = price;
        this.totalCapacity = totalCapacity;
        this.availableSeats = availableSeats;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public int getEventSectionId() {
        return eventSectionId;
    }
    
    public void setEventSectionId(int eventSectionId) {
        this.eventSectionId = eventSectionId;
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
    
    public String getSectionTitle() {
        return sectionTitle;
    }
    
    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public int getTotalCapacity() {
        return totalCapacity;
    }
    
    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
    
    public int getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getSectionName() {
        return sectionName;
    }
    
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
    
    public String getSectionType() {
        return sectionType;
    }
    
    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
    }
    
    // Helper methods
    public int getBookedSeats() {
        return totalCapacity - availableSeats;
    }
    
    public double getOccupancyRate() {
        if (totalCapacity == 0) return 0.0;
        return (double) getBookedSeats() / totalCapacity * 100;
    }
    
    public boolean isFullyBooked() {
        return availableSeats <= 0;
    }
    
    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }
    
    @Override
    public String toString() {
        return sectionTitle + " - Rp " + String.format("%.2f", price);
    }
}
