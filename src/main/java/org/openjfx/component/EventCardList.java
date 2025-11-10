package org.openjfx.component;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.openjfx.model.Event;
import org.openjfx.service.EventService;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * A scrollable container that displays a list of EventCard components.
 * Features lazy loading, filtering, sorting, and dynamic updates.
 */
public class EventCardList extends VBox {
   
   private final EventService eventService;
   private VBox cardContainer;
   private ScrollPane scrollPane;
   private Label emptyStateLabel;
   private List<Event> currentEvents;
   private Consumer<Event> onEventClickHandler;
   
   /**
    * Creates an EventCardList with default EventService
    */
   public EventCardList() {
      this(new EventService());
   }
   
   /**
    * Creates an EventCardList with a custom EventService
    * 
    * @param eventService The service to use for fetching events
    */
   public EventCardList(EventService eventService) {
      super(10);
      this.eventService = eventService;
      initializeUI();
      loadEvents();
   }
   
   /**
    * Initialize the UI components
    */
   private void initializeUI() {
      // Card container
      cardContainer = new VBox(15);
      cardContainer.setPadding(new Insets(10));
      
      // Empty state label
      emptyStateLabel = createEmptyStateLabel();
      
      // Scroll pane
      scrollPane = new ScrollPane(cardContainer);
      scrollPane.setFitToWidth(true);
      scrollPane.setStyle(
         "-fx-background-color: transparent; " +
         "-fx-background: #f5f5f5; " +
         "-fx-border-color: transparent;"
      );
      scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
      
      this.getChildren().add(scrollPane);
      this.setStyle("-fx-background-color: #f5f5f5;");
   }
   
   /**
    * Create empty state label for when no events are available
    */
   private Label createEmptyStateLabel() {
      Label label = new Label("No events available");
      label.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
      label.setStyle("-fx-text-fill: #7f8c8d; -fx-padding: 40;");
      label.setAlignment(Pos.CENTER);
      return label;
   }
   
   /**
    * Load all events from the service
    */
   private void loadEvents() {
      // Run in background to avoid blocking UI
      new Thread(() -> {
         try {
            List<Event> events = eventService.getAllEvents();
            
            // Update UI on JavaFX Application Thread
            Platform.runLater(() -> {
               currentEvents = events;
               displayEvents(events);
            });
         } catch (Exception e) {
            Platform.runLater(() -> showError("Failed to load events: " + e.getMessage()));
            e.printStackTrace();
         }
      }).start();
   }
   
   /**
    * Display the list of events as cards
    */
   private void displayEvents(List<Event> events) {
      cardContainer.getChildren().clear();
      
      if (events == null || events.isEmpty()) {
         cardContainer.getChildren().add(emptyStateLabel);
         return;
      }
      
      for (Event event : events) {
         EventCard card = createEventCard(event);
         cardContainer.getChildren().add(card);
      }
   }
   
   /**
    * Create an EventCard for the given event
    */
   private EventCard createEventCard(Event event) {
      FontAwesomeIcon icon = getIconForEventType(event.getEventType());
      EventCard card = new EventCard(event, icon);
      
      // Set click handler if one is defined
      if (onEventClickHandler != null) {
         card.setOnMouseClicked(e -> onEventClickHandler.accept(event));
      }
      
      return card;
   }
   
   /**
    * Get appropriate icon based on event type
    */
   private FontAwesomeIcon getIconForEventType(String eventType) {
      if (eventType == null) return FontAwesomeIcon.CALENDAR;
      
      switch (eventType.toLowerCase()) {
         case "sports":
         case "sport":
            return FontAwesomeIcon.FUTBOL_ALT;
         case "concert":
         case "music":
            return FontAwesomeIcon.MUSIC;
         case "conference":
         case "meeting":
            return FontAwesomeIcon.USERS;
         case "exhibition":
         case "show":
            return FontAwesomeIcon.EYE;
         case "theater":
         case "drama":
            return FontAwesomeIcon.TICKET;
         default:
            return FontAwesomeIcon.CALENDAR;
      }
   }
   
   /**
    * Refresh the event list from the database
    */
   public void refresh() {
      loadEvents();
   }
   
   /**
    * Filter events by status
    * 
    * @param status The status to filter by (null shows all)
    */
   public void filterByStatus(String status) {
      if (currentEvents == null) return;
      
      List<Event> filtered = currentEvents;
      if (status != null && !status.isEmpty()) {
         filtered = currentEvents.stream()
            .filter(e -> status.equalsIgnoreCase(e.getStatus()))
            .collect(Collectors.toList());
      }
      
      displayEvents(filtered);
   }
   
   /**
    * Filter events by type
    * 
    * @param eventType The event type to filter by (null shows all)
    */
   public void filterByType(String eventType) {
      if (currentEvents == null) return;
      
      List<Event> filtered = currentEvents;
      if (eventType != null && !eventType.isEmpty()) {
         filtered = currentEvents.stream()
            .filter(e -> eventType.equalsIgnoreCase(e.getEventType()))
            .collect(Collectors.toList());
      }
      
      displayEvents(filtered);
   }
   
   /**
    * Search events by name or description
    * 
    * @param searchTerm The term to search for
    */
   public void search(String searchTerm) {
      if (currentEvents == null) return;
      
      if (searchTerm == null || searchTerm.trim().isEmpty()) {
         displayEvents(currentEvents);
         return;
      }
      
      String lowerCaseSearch = searchTerm.toLowerCase();
      List<Event> filtered = currentEvents.stream()
         .filter(e -> 
            (e.getEventName() != null && e.getEventName().toLowerCase().contains(lowerCaseSearch)) ||
            (e.getDescription() != null && e.getDescription().toLowerCase().contains(lowerCaseSearch))
         )
         .collect(Collectors.toList());
      
      displayEvents(filtered);
   }
   
   /**
    * Sort events by name
    * 
    * @param ascending true for A-Z, false for Z-A
    */
   public void sortByName(boolean ascending) {
      if (currentEvents == null) return;
      
      List<Event> sorted = currentEvents.stream()
         .sorted((e1, e2) -> {
            int comparison = e1.getEventName().compareToIgnoreCase(e2.getEventName());
            return ascending ? comparison : -comparison;
         })
         .collect(Collectors.toList());
      
      displayEvents(sorted);
   }
   
   /**
    * Sort events by booked seats
    * 
    * @param ascending true for low to high, false for high to low
    */
   public void sortBySeats(boolean ascending) {
      if (currentEvents == null) return;
      
      List<Event> sorted = currentEvents.stream()
         .sorted((e1, e2) -> {
            int comparison = Integer.compare(e1.getBookedSeats(), e2.getBookedSeats());
            return ascending ? comparison : -comparison;
         })
         .collect(Collectors.toList());
      
      displayEvents(sorted);
   }
   
   /**
    * Set a click handler for event cards
    * 
    * @param handler Consumer that handles event clicks
    */
   public void setOnEventClick(Consumer<Event> handler) {
      this.onEventClickHandler = handler;
      // Update existing cards
      if (currentEvents != null) {
         displayEvents(currentEvents);
      }
   }
   
   /**
    * Get the current list of events
    */
   public List<Event> getCurrentEvents() {
      return currentEvents;
   }
   
   /**
    * Clear all events from the display
    */
   public void clear() {
      cardContainer.getChildren().clear();
      currentEvents = null;
   }
   
   /**
    * Show error message
    */
   private void showError(String message) {
      Label errorLabel = new Label(message);
      errorLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
      errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-padding: 20;");
      errorLabel.setWrapText(true);
      
      cardContainer.getChildren().clear();
      cardContainer.getChildren().add(errorLabel);
   }
   
   /**
    * Add a single event to the list
    * 
    * @param event The event to add
    */
   public void addEvent(Event event) {
      if (event == null) return;
      
      if (currentEvents == null) {
         loadEvents();
         return;
      }
      
      currentEvents.add(event);
      EventCard card = createEventCard(event);
      
      // Remove empty state if present
      if (cardContainer.getChildren().contains(emptyStateLabel)) {
         cardContainer.getChildren().remove(emptyStateLabel);
      }
      
      cardContainer.getChildren().add(card);
   }
   
   /**
    * Remove an event from the list
    * 
    * @param event The event to remove
    */
   public void removeEvent(Event event) {
      if (event == null || currentEvents == null) return;
      
      currentEvents.removeIf(e -> e.getId() == event.getId());
      displayEvents(currentEvents);
   }
   
   /**
    * Update an event in the list
    * 
    * @param event The updated event
    */
   public void updateEvent(Event event) {
      if (event == null || currentEvents == null) return;
      
      // Update in the list
      for (int i = 0; i < currentEvents.size(); i++) {
         if (currentEvents.get(i).getId() == event.getId()) {
            currentEvents.set(i, event);
            break;
         }
      }
      
      displayEvents(currentEvents);
   }
}
