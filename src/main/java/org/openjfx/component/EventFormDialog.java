package org.openjfx.component;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.openjfx.model.Event;
import org.openjfx.service.EventService;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Event Form Dialog Component
 * Create or edit event dialog
 */
public class EventFormDialog {
    
    private final Stage dialog;
    private final Event existingEvent;
    private final Runnable onSuccess;
    private final EventService eventService;
    
    private TextField nameField;
    private ComboBox<String> typeCombo;
    private DatePicker datePicker;
    private TextField timeField;
    private TextField seatsField;
    private ComboBox<String> statusCombo;
    private TextArea descArea;
    private Label errorLabel;
    private Button saveBtn;  // Add reference to save button
    
    public EventFormDialog(Stage owner, Event existingEvent, Runnable onSuccess) {
        this.dialog = new Stage();
        this.existingEvent = existingEvent;
        this.onSuccess = onSuccess;
        this.eventService = new EventService();
        
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(existingEvent == null ? "Create New Event" : "Edit Event");
        dialog.setResizable(false);
        
        createForm();
    }
    
    private void createForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label(existingEvent == null ? "Create New Event" : "Edit Event");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Event Name
        Label nameLabel = new Label("Event Name *");
        nameField = new TextField();
        nameField.setPromptText("Enter event name");
        if (existingEvent != null) nameField.setText(existingEvent.getEventName());

        // Event Type
        Label typeLabel = new Label("Event Type *");
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Football Match", "Concert", "Basketball", "Conference", "Other");
        if (existingEvent != null) {
            typeCombo.setValue(existingEvent.getEventType());
        } else {
            typeCombo.setValue("Football Match");
        }
        typeCombo.setPrefWidth(Double.MAX_VALUE);

        // Event Date
        Label dateLabel = new Label("Event Date *");
        datePicker = new DatePicker();
        if (existingEvent != null) {
            datePicker.setValue(existingEvent.getEventDate());
        } else {
            datePicker.setValue(LocalDate.now());
        }

        // Event Time
        Label timeLabel = new Label("Event Time *");
        timeField = new TextField();
        timeField.setPromptText("HH:mm (e.g., 19:00)");
        if (existingEvent != null) timeField.setText(existingEvent.getEventTime().toString());

        // Total Seats
        Label seatsLabel = new Label("Total Seats *");
        seatsField = new TextField();
        seatsField.setPromptText("Enter total seats");
        if (existingEvent != null) seatsField.setText(String.valueOf(existingEvent.getTotalSeats()));

        // Status
        Label statusLabel = new Label("Status *");
        statusCombo = new ComboBox<>();
        // Database constraint requires: 'Active', 'Cancelled', 'Completed'
        statusCombo.getItems().addAll("Active", "Cancelled", "Completed");
        if (existingEvent != null) {
            statusCombo.setValue(existingEvent.getStatus());
        } else {
            statusCombo.setValue("Active");
        }
        statusCombo.setPrefWidth(Double.MAX_VALUE);

        // Description
        Label descLabel = new Label("Description");
        descArea = new TextArea();
        descArea.setPromptText("Enter event description");
        descArea.setPrefRowCount(4);
        if (existingEvent != null) descArea.setText(existingEvent.getDescription());

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn = new Button(existingEvent == null ? "Create" : "Update");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-padding: 8 20; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> handleSave());

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        form.getChildren().addAll(titleLabel, nameLabel, nameField, typeLabel, typeCombo,
                                  dateLabel, datePicker, timeLabel, timeField,
                                  seatsLabel, seatsField, statusLabel, statusCombo,
                                  descLabel, descArea, errorLabel, buttonBox);

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white;");
        
        Scene scene = new Scene(scrollPane, 550, 650);
        dialog.setScene(scene);
        
        dialog.setOnShown(e -> nameField.requestFocus());
    }
    
    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            errorLabel.setText("Event name is required");
            return false;
        }

        if (timeField.getText().trim().isEmpty()) {
            errorLabel.setText("Event time is required");
            return false;
        }

        try {
            LocalTime.parse(timeField.getText().trim());
        } catch (Exception e) {
            errorLabel.setText("Invalid time format. Use HH:mm (e.g., 19:00)");
            return false;
        }

        if (seatsField.getText().trim().isEmpty()) {
            errorLabel.setText("Total seats is required");
            return false;
        }

        try {
            int seats = Integer.parseInt(seatsField.getText().trim());
            if (seats <= 0) {
                errorLabel.setText("Total seats must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid number for total seats");
            return false;
        }

        return true;
    }
    
    private void handleSave() {
        if (!validateForm()) return;
        
        System.out.println("DEBUG: handleSave called - starting save process");
        
        LocalTime time = LocalTime.parse(timeField.getText().trim());
        int totalSeats = Integer.parseInt(seatsField.getText().trim());

        if (existingEvent == null) {
            // Creating new event - run in background thread
            System.out.println("DEBUG: Creating new event in background thread");
            
            // Map combo box value to database event type
            String eventType = typeCombo.getValue().toUpperCase().contains("FOOTBALL") ? "Football" : "Concert";
            
            Event newEvent = new Event(
                nameField.getText(),
                eventType,
                datePicker.getValue(),
                time,
                descArea.getText(),
                totalSeats
            );
            newEvent.setStatus(statusCombo.getValue());
            
            // Disable form while saving
            saveBtn.setDisable(true);
            saveBtn.setText("Creating...");
            System.out.println("DEBUG: Button disabled, showing 'Creating...'");
            
            new Thread(() -> {
                System.out.println("DEBUG: Background thread started, calling createEvent");
                boolean success = eventService.createEvent(newEvent); // This sets the ID on newEvent
                System.out.println("DEBUG: createEvent returned: " + success);
                
                Platform.runLater(() -> {
                    System.out.println("DEBUG: Back on UI thread with Platform.runLater");
                    if (success) {
                        System.out.println("DEBUG: Event created successfully with ID: " + newEvent.getId());
                        dialog.close();
                        
                        // Automatically open section configuration dialog
                        System.out.println("DEBUG: Opening EventSectionConfigDialog for event ID: " + newEvent.getId());
                        
                        EventSectionConfigDialog configDialog = new EventSectionConfigDialog(
                            newEvent.getId(), // ID was set by createEvent
                            eventType,
                            totalSeats // Pass the totalSeats we already have
                        );
                        
                        // When section config is complete, refresh the event list
                        configDialog.setOnSaveComplete(() -> {
                            System.out.println("DEBUG: Section configuration completed");
                            if (onSuccess != null) {
                                onSuccess.run();
                            }
                        });
                        
                        // If user closes the config dialog without saving, still refresh
                        configDialog.setOnHidden(e -> {
                            System.out.println("DEBUG: Section config dialog closed");
                            if (onSuccess != null) {
                                onSuccess.run();
                            }
                        });
                        
                        configDialog.show();
                    } else {
                        // Re-enable form on failure
                        saveBtn.setDisable(false);
                        saveBtn.setText("Save");
                        errorLabel.setText("Failed to create event. Please try again.");
                    }
                });
            }).start();
            
        } else {
            // Updating existing event - also run in background
            
            // Map combo box value to database event type
            String eventType = typeCombo.getValue().toUpperCase().contains("FOOTBALL") ? "Football" : "Concert";
            
            existingEvent.setEventName(nameField.getText());
            existingEvent.setEventType(eventType);
            existingEvent.setEventDate(datePicker.getValue());
            existingEvent.setEventTime(time);
            existingEvent.setTotalSeats(totalSeats);
            existingEvent.setStatus(statusCombo.getValue());
            existingEvent.setDescription(descArea.getText());
            
            saveBtn.setDisable(true);
            saveBtn.setText("Updating...");
            
            new Thread(() -> {
                boolean success = eventService.updateEvent(existingEvent);
                
                Platform.runLater(() -> {
                    if (success) {
                        dialog.close();
                        onSuccess.run();
                    } else {
                        saveBtn.setDisable(false);
                        saveBtn.setText("Save");
                        errorLabel.setText("Failed to update event. Please try again.");
                    }
                });
            }).start();
        }
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
