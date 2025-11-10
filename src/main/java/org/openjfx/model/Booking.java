package org.openjfx.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a customer booking
 */
public class Booking {
    private int bookingId;
    private int eventId;
    private String bookingNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private int totalSeats;
    private double totalPrice;
    private String bookingStatus; // CONFIRMED, CANCELLED
    private Timestamp bookingDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display
    private String eventName;
    private List<BookingSeat> bookingSeats;
    
    // Constructors
    public Booking() {
        this.bookingSeats = new ArrayList<>();
    }
    
    public Booking(int bookingId, int eventId, String bookingNumber,
                   String customerName, String customerEmail, String customerPhone,
                   int totalSeats, double totalPrice, String bookingStatus,
                   Timestamp bookingDate, Timestamp createdAt, Timestamp updatedAt) {
        this.bookingId = bookingId;
        this.eventId = eventId;
        this.bookingNumber = bookingNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.totalSeats = totalSeats;
        this.totalPrice = totalPrice;
        this.bookingStatus = bookingStatus;
        this.bookingDate = bookingDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bookingSeats = new ArrayList<>();
    }
    
    // Getters and Setters
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
    
    public String getBookingNumber() {
        return bookingNumber;
    }
    
    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public int getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getBookingStatus() {
        return bookingStatus;
    }
    
    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
    
    public Timestamp getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(Timestamp bookingDate) {
        this.bookingDate = bookingDate;
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
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public List<BookingSeat> getBookingSeats() {
        return bookingSeats;
    }
    
    public void setBookingSeats(List<BookingSeat> bookingSeats) {
        this.bookingSeats = bookingSeats;
    }
    
    // Helper methods
    public boolean isConfirmed() {
        return "CONFIRMED".equals(bookingStatus);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(bookingStatus);
    }
    
    @Override
    public String toString() {
        return bookingNumber + " - " + customerName;
    }
}
