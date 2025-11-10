package org.openjfx.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

public class Event {
    private int id;
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String description;
    private String status; // UPCOMING, ONGOING, COMPLETED, CANCELLED
    private int totalSeats;
    private int bookedSeats;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public Event() {
        this.status = "UPCOMING";
        this.bookedSeats = 0;
    }

    public Event(String eventName, String eventType, LocalDate eventDate, LocalTime eventTime, 
                 String description, int totalSeats) {
        this();
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.description = description;
        this.totalSeats = totalSeats;
    }

    // Full constructor (for database retrieval)
    public Event(int id, String eventName, String eventType, LocalDate eventDate, LocalTime eventTime,
                 String description, String status, int totalSeats, int bookedSeats,
                 Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.description = description;
        this.status = status;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
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

    // Computed properties
    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }

    public double getOccupancyRate() {
        if (totalSeats == 0) return 0.0;
        return (double) bookedSeats / totalSeats * 100;
    }

    public boolean isFullyBooked() {
        return bookedSeats >= totalSeats;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", eventName='" + eventName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventDate=" + eventDate +
                ", eventTime=" + eventTime +
                ", status='" + status + '\'' +
                ", totalSeats=" + totalSeats +
                ", bookedSeats=" + bookedSeats +
                ", availableSeats=" + getAvailableSeats() +
                '}';
    }
}
