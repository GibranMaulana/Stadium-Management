package org.openjfx.util;

import org.openjfx.model.Event;
import org.openjfx.service.EventService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Test utility to verify Events Management functionality
 */
public class TestEvents {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Events Management System ===\n");
        
        EventService eventService = new EventService();
        
        // Test 1: Get all events
        System.out.println("Test 1: Get All Events");
        System.out.println("------------------------");
        List<Event> allEvents = eventService.getAllEvents();
        System.out.println("Found " + allEvents.size() + " events:");
        for (Event event : allEvents) {
            System.out.println("  - " + event.getEventName() + 
                             " (" + event.getEventType() + ") - " + 
                             event.getEventDate() + " at " + event.getEventTime());
            System.out.println("    Status: " + event.getStatus() + 
                             ", Seats: " + event.getBookedSeats() + "/" + event.getTotalSeats() +
                             " (" + event.getAvailableSeats() + " available)");
        }
        System.out.println();
        
        // Test 2: Search events
        System.out.println("Test 2: Search Events (keyword: 'concert')");
        System.out.println("------------------------------------------");
        List<Event> searchResults = eventService.searchEvents("concert");
        System.out.println("Found " + searchResults.size() + " matching events:");
        for (Event event : searchResults) {
            System.out.println("  - " + event.getEventName());
        }
        System.out.println();
        
        // Test 3: Filter by type
        System.out.println("Test 3: Filter by Type ('Football Match')");
        System.out.println("------------------------------------------");
        List<Event> footballEvents = eventService.filterEventsByType("Football Match");
        System.out.println("Found " + footballEvents.size() + " football matches:");
        for (Event event : footballEvents) {
            System.out.println("  - " + event.getEventName() + " on " + event.getEventDate());
        }
        System.out.println();
        
        // Test 4: Get upcoming events
        System.out.println("Test 4: Get Upcoming Events");
        System.out.println("----------------------------");
        List<Event> upcomingEvents = eventService.getUpcomingEvents();
        System.out.println("Found " + upcomingEvents.size() + " upcoming events:");
        for (Event event : upcomingEvents) {
            System.out.println("  - " + event.getEventName() + " - " + event.getEventDate());
        }
        System.out.println();
        
        // Test 5: Create new event
        System.out.println("Test 5: Create New Event");
        System.out.println("------------------------");
        Event newEvent = new Event(
            "Tennis Championship",
            "Tennis",
            LocalDate.of(2025, 12, 1),
            LocalTime.of(14, 0),
            "International tennis championship finals",
            10000
        );
        boolean created = eventService.createEvent(newEvent);
        if (created) {
            System.out.println("✓ Event created successfully with ID: " + newEvent.getId());
        } else {
            System.out.println("✗ Failed to create event");
        }
        System.out.println();
        
        // Test 6: Get event by ID
        if (created && newEvent.getId() > 0) {
            System.out.println("Test 6: Get Event by ID");
            System.out.println("------------------------");
            Event retrieved = eventService.getEventById(newEvent.getId());
            if (retrieved != null) {
                System.out.println("✓ Retrieved event: " + retrieved.getEventName());
                System.out.println("  Type: " + retrieved.getEventType());
                System.out.println("  Date: " + retrieved.getEventDate() + " at " + retrieved.getEventTime());
                System.out.println("  Capacity: " + retrieved.getTotalSeats() + " seats");
            } else {
                System.out.println("✗ Failed to retrieve event");
            }
            System.out.println();
            
            // Test 7: Update event
            System.out.println("Test 7: Update Event");
            System.out.println("--------------------");
            retrieved.setStatus("ONGOING");
            retrieved.setDescription("Live now! International tennis championship finals");
            boolean updated = eventService.updateEvent(retrieved);
            if (updated) {
                System.out.println("✓ Event updated successfully");
                Event updatedEvent = eventService.getEventById(retrieved.getId());
                System.out.println("  New status: " + updatedEvent.getStatus());
                System.out.println("  New description: " + updatedEvent.getDescription());
            } else {
                System.out.println("✗ Failed to update event");
            }
            System.out.println();
            
            // Test 8: Check for bookings
            System.out.println("Test 8: Check for Bookings");
            System.out.println("--------------------------");
            boolean hasBookings = eventService.hasBookings(newEvent.getId());
            System.out.println("Event has bookings: " + hasBookings);
            System.out.println();
            
            // Test 9: Delete event
            System.out.println("Test 9: Delete Event");
            System.out.println("--------------------");
            boolean deleted = eventService.deleteEvent(newEvent.getId());
            if (deleted) {
                System.out.println("✓ Event deleted successfully");
            } else {
                System.out.println("✗ Failed to delete event (may have bookings)");
            }
            System.out.println();
        }
        
        // Test 10: Get event count
        System.out.println("Test 10: Get Event Count");
        System.out.println("------------------------");
        int count = eventService.getEventCount();
        System.out.println("Total events in database: " + count);
        System.out.println();
        
        // Test 11: Get event types
        System.out.println("Test 11: Get Event Types");
        System.out.println("------------------------");
        List<String> types = eventService.getEventTypes();
        System.out.println("Available event types: " + types);
        System.out.println();
        
        System.out.println("=== All Tests Completed ===");
    }
}
