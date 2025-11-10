package org.openjfx.component;

import org.openjfx.model.Event;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * A card component that displays event information in a horizontal layout.
 * Features hover effects, status-based styling, and click interactions.
 */
public class EventCard extends HBox {
   
   private static final String BASE_STYLE = 
      "-fx-background-color: white; " +
      "-fx-background-radius: 8; " +
      "-fx-border-color: #e0e0e0; " +
      "-fx-border-radius: 8; " +
      "-fx-border-width: 1; " +
      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);";
   
   private static final String HOVER_STYLE = 
      "-fx-background-color: #f8f9fa; " +
      "-fx-background-radius: 8; " +
      "-fx-border-color: #3498db; " +
      "-fx-border-radius: 8; " +
      "-fx-border-width: 2; " +
      "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 10, 0, 0, 3);";
   
   private final Event event;
   private FontAwesomeIconView iconView;
   private Label titleLabel;
   private Label eventTypeLabel;
   private Label descriptionLabel;
   private Label statusLabel;
   private Label seatLabel;
   
   public EventCard(
         FontAwesomeIcon icon, 
         String title, 
         String eventType,
         String description,
         String status,
         int occupiedSeats) {
      this(null, icon, title, eventType, description, status, occupiedSeats);
   }
   
   /**
    * Creates an EventCard with the Event model object.
    * 
    * @param event The Event model object
    * @param icon FontAwesome icon to display
    */
   public EventCard(Event event, FontAwesomeIcon icon) {
      this(
         event,
         icon,
         event.getEventName(),
         event.getEventType(),
         event.getDescription(),
         event.getStatus(),
         event.getBookedSeats()
      );
   }
   
   private EventCard(
         Event event,
         FontAwesomeIcon icon, 
         String title, 
         String eventType,
         String description,
         String status,
         int occupiedSeats) {
      super(15);
      this.event = event;
      
      initializeUI(icon, title, eventType, description, status, occupiedSeats);
      applyStyles();
      setupInteractions();
   }
   
   /**
    * Initialize all UI components
    */
   private void initializeUI(
         FontAwesomeIcon icon,
         String title,
         String eventType,
         String description,
         String status,
         int occupiedSeats) {
      
      // Icon
      iconView = new FontAwesomeIconView(icon);
      iconView.setSize("32");
      iconView.setStyle("-fx-fill: " + getIconColor(status) + ";");
      
      // Left side content
      VBox left = createLeftSide(title, eventType, description);
      
      // Spacer
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      
      // Right side content
      VBox right = createRightSide(status, occupiedSeats);
      
      this.getChildren().addAll(iconView, left, spacer, right);
      this.setPadding(new Insets(20));
      this.setAlignment(Pos.CENTER_LEFT);
   }
   
   /**
    * Create the left side of the card with event details
    */
   private VBox createLeftSide(String title, String eventType, String description) {
      VBox container = new VBox(8);
      container.setAlignment(Pos.CENTER_LEFT);
      
      titleLabel = new Label(sanitizeText(title));
      titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
      titleLabel.setStyle("-fx-text-fill: #2c3e50;");
      titleLabel.setWrapText(false);
      titleLabel.setMaxWidth(400);
      
      eventTypeLabel = new Label(sanitizeText(eventType));
      eventTypeLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
      eventTypeLabel.setStyle(
         "-fx-text-fill: #3498db; " +
         "-fx-background-color: #e3f2fd; " +
         "-fx-padding: 4 8 4 8; " +
         "-fx-background-radius: 4;"
      );
      
      descriptionLabel = new Label(sanitizeText(description));
      descriptionLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
      descriptionLabel.setStyle("-fx-text-fill: #7f8c8d;");
      descriptionLabel.setWrapText(true);
      descriptionLabel.setMaxWidth(400);
      
      container.getChildren().addAll(titleLabel, eventTypeLabel, descriptionLabel);
      return container;
   }
   
   /**
    * Create the right side of the card with status and seats
    */
   private VBox createRightSide(String status, int occupiedSeats) {
      VBox container = new VBox(8);
      container.setAlignment(Pos.CENTER_RIGHT);
      container.setPadding(new Insets(0, 10, 0, 0));
      
      statusLabel = new Label(sanitizeText(status));
      statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
      statusLabel.setStyle(
         "-fx-text-fill: " + getStatusColor(status) + "; " +
         "-fx-background-color: " + getStatusBackgroundColor(status) + "; " +
         "-fx-padding: 8 16 8 16; " +
         "-fx-background-radius: 20;"
      );
      
      HBox seatInfo = new HBox(14);
      seatInfo.setAlignment(Pos.CENTER_RIGHT);
      
      FontAwesomeIconView seatIcon = new FontAwesomeIconView(FontAwesomeIcon.USERS);
      seatIcon.setSize("14");
      seatIcon.setStyle("-fx-fill: #7f8c8d;");
      
      seatLabel = new Label(String.valueOf(occupiedSeats) + " seats");
      seatLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
      seatLabel.setStyle("-fx-text-fill: #7f8c8d;");
      
      seatInfo.getChildren().addAll(seatIcon, seatLabel);
      
      container.getChildren().addAll(statusLabel, seatInfo);
      return container;
   }
   
   /**
    * Apply base styling to the card
    */
   private void applyStyles() {
      this.setStyle(BASE_STYLE);
      this.setMinHeight(120);
      this.setMaxHeight(150);
      this.setPrefHeight(130);
   }
   
   /**
    * Setup mouse interactions (hover effects, click handlers)
    */
   private void setupInteractions() {
      this.setCursor(Cursor.HAND);
      
      // Hover effects
      this.setOnMouseEntered(e -> this.setStyle(HOVER_STYLE));
      this.setOnMouseExited(e -> this.setStyle(BASE_STYLE));
      
      // Click handler - can be customized by external code
      this.setOnMouseClicked(e -> {
         System.out.println("EventCard clicked: " + titleLabel.getText());
         // This can be overridden by setting a new handler externally
      });
   }
   
   /**
    * Get the associated Event model object
    */
   public Event getEvent() {
      return event;
   }
   
   /**
    * Update the card with new data
    */
   public void updateData(String title, String eventType, String description, String status, int occupiedSeats) {
      titleLabel.setText(sanitizeText(title));
      eventTypeLabel.setText(sanitizeText(eventType));
      descriptionLabel.setText(sanitizeText(description));
      statusLabel.setText(sanitizeText(status));
      statusLabel.setStyle(
         "-fx-text-fill: " + getStatusColor(status) + "; " +
         "-fx-background-color: " + getStatusBackgroundColor(status) + "; " +
         "-fx-padding: 8 16 8 16; " +
         "-fx-background-radius: 20;"
      );
      seatLabel.setText(String.valueOf(occupiedSeats) + " seats");
   }
   
   /**
    * Get icon color based on status
    */
   private String getIconColor(String status) {
      if (status == null) return "#95a5a6";
      
      switch (status.toLowerCase()) {
         case "active":
         case "ongoing":
            return "#27ae60";
         case "upcoming":
         case "scheduled":
            return "#3498db";
         case "completed":
         case "finished":
            return "#95a5a6";
         case "cancelled":
            return "#e74c3c";
         default:
            return "#95a5a6";
      }
   }
   
   /**
    * Get status text color
    */
   private String getStatusColor(String status) {
      if (status == null) return "#7f8c8d";
      
      switch (status.toLowerCase()) {
         case "active":
         case "ongoing":
            return "#27ae60";
         case "upcoming":
         case "scheduled":
            return "#2980b9";
         case "completed":
         case "finished":
            return "#7f8c8d";
         case "cancelled":
            return "#c0392b";
         default:
            return "#7f8c8d";
      }
   }
   
   /**
    * Get status background color
    */
   private String getStatusBackgroundColor(String status) {
      if (status == null) return "#ecf0f1";
      
      switch (status.toLowerCase()) {
         case "active":
         case "ongoing":
            return "#d4edda";
         case "upcoming":
         case "scheduled":
            return "#d1ecf1";
         case "completed":
         case "finished":
            return "#e9ecef";
         case "cancelled":
            return "#f8d7da";
         default:
            return "#ecf0f1";
      }
   }
   
   /**
    * Sanitize text to prevent null or empty values
    */
   private String sanitizeText(String text) {
      return (text == null || text.trim().isEmpty()) ? "N/A" : text;
   }
}
