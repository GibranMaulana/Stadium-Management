package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.openjfx.service.EventService;

/**
 * A section that displays multiple statistics cards with real-time data.
 * Automatically loads and refreshes statistics from the database.
 */
public class StatsSection extends HBox {
    
    private final EventService eventService;
    
    private StatCard eventsCard;
    private StatCard bookingsCard;
    private StatCard revenueCard;
    private StatCard seatsCard;
    
    /**
     * Creates a StatsSection with default services
     */
    public StatsSection() {
        this(new EventService());
    }
    
    /**
     * Creates a StatsSection with custom services
     * 
     * @param eventService Service for event operations
     */
    public StatsSection(EventService eventService) {
        super(20);
        this.eventService = eventService;
        
        initializeUI();
        loadStatistics();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(0));
        
        // Create stat cards with loading state
        eventsCard = new StatCard(
            FontAwesomeIcon.CALENDAR, 
            "Total Events", 
            "Loading...", 
            "Active events",
            "#3498db"
        );
        
        bookingsCard = new StatCard(
            FontAwesomeIcon.TICKET, 
            "Today's Bookings", 
            "Loading...",
            "New bookings today",
            "#2ecc71"
        );
        
        revenueCard = new StatCard(
            FontAwesomeIcon.DOLLAR, 
            "Total Revenue", 
            "Loading...",
            "All time earnings",
            "#f39c12"
        );
        
        seatsCard = new StatCard(
            FontAwesomeIcon.TH, 
            "Available Seats", 
            "Loading...",
            "Across all events",
            "#9b59b6"
        );
        
        // Make cards grow equally
        HBox.setHgrow(eventsCard, Priority.ALWAYS);
        HBox.setHgrow(bookingsCard, Priority.ALWAYS);
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        HBox.setHgrow(seatsCard, Priority.ALWAYS);
        
        this.getChildren().addAll(eventsCard, bookingsCard, revenueCard, seatsCard);
    }
    
    /**
     * Load statistics from the database in background thread
     */
    private void loadStatistics() {
        new Thread(() -> {
            try {
                // Get total events count
                int totalEvents = eventService.getAllEvents().size();
                
                // Get active events count
                long activeEvents = eventService.getAllEvents().stream()
                    .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()) || 
                                 "ONGOING".equalsIgnoreCase(e.getStatus()) ||
                                 "UPCOMING".equalsIgnoreCase(e.getStatus()))
                    .count();
                
                // Get total booked seats across all events
                int totalBookedSeats = eventService.getAllEvents().stream()
                    .mapToInt(e -> e.getBookedSeats())
                    .sum();
                
                // Calculate total revenue (estimate based on seat price)
                double estimatedRevenue = totalBookedSeats * 50.0; // $50 per seat estimate
                
                // Get total available seats
                int totalSeats = eventService.getAllEvents().stream()
                    .mapToInt(e -> e.getTotalSeats())
                    .sum();
                int availableSeats = totalSeats - totalBookedSeats;
                
                // Calculate occupancy rate
                double occupancyRate = totalSeats > 0 ? (totalBookedSeats * 100.0 / totalSeats) : 0;
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    eventsCard.updateValue(String.valueOf(totalEvents));
                    eventsCard.updateSubtitle(activeEvents + " active");
                    
                    bookingsCard.updateValue(String.valueOf(totalBookedSeats));
                    bookingsCard.updateSubtitle("Total bookings");
                    
                    revenueCard.updateValue(String.format("$%.2f", estimatedRevenue));
                    revenueCard.updateSubtitle("Estimated revenue");
                    
                    seatsCard.updateValue(String.valueOf(availableSeats));
                    seatsCard.updateSubtitle(String.format("%.1f%% occupied", occupancyRate));
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                
                // Show error state
                Platform.runLater(() -> {
                    eventsCard.updateValue("0");
                    eventsCard.updateSubtitle("No data");
                    
                    bookingsCard.updateValue("0");
                    bookingsCard.updateSubtitle("No data");
                    
                    revenueCard.updateValue("$0.00");
                    revenueCard.updateSubtitle("No data");
                    
                    seatsCard.updateValue("0");
                    seatsCard.updateSubtitle("No data");
                });
            }
        }).start();
    }
    
    /**
     * Refresh all statistics from the database
     */
    public void refresh() {
        eventsCard.updateValue("Loading...");
        bookingsCard.updateValue("Loading...");
        revenueCard.updateValue("Loading...");
        seatsCard.updateValue("Loading...");
        
        loadStatistics();
    }
    
    /**
     * Get the events card
     */
    public StatCard getEventsCard() {
        return eventsCard;
    }
    
    /**
     * Get the bookings card
     */
    public StatCard getBookingsCard() {
        return bookingsCard;
    }
    
    /**
     * Get the revenue card
     */
    public StatCard getRevenueCard() {
        return revenueCard;
    }
    
    /**
     * Get the seats card
     */
    public StatCard getSeatsCard() {
        return seatsCard;
    }
}
