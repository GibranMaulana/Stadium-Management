package org.openjfx.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.openjfx.model.Event;
import org.openjfx.model.EventSection;
import org.openjfx.model.Section;
import org.openjfx.service.EventService;
import org.openjfx.service.EventSectionService;
import org.openjfx.service.SectionService;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified event creation form with integrated section configuration
 */
public class EventFormView extends VBox {
    
    private final EventService eventService;
    private final SectionService sectionService;
    private final EventSectionService eventSectionService;
    private final Event existingEvent;
    private final Runnable onSuccess;
    
    // Event Details Fields
    private TextField nameField;
    private ComboBox<String> typeCombo;
    private DatePicker datePicker;
    private TextField timeField;
    private TextArea descArea;
    private ComboBox<String> statusCombo;
    
    // Calculated max capacity
    private int maxCapacity = 0;
    
    // Section Configuration
    private VBox sectionsContainer;
    private List<SectionRow> sectionRows;
    private Label totalSeatsLabel;
    
    // Action Buttons
    private Button saveButton;
    private Button cancelButton;
    private Label errorLabel;
    
    public EventFormView(Event existingEvent, Runnable onSuccess) {
        this.eventService = new EventService();
        this.sectionService = new SectionService();
        this.eventSectionService = new EventSectionService();
        this.existingEvent = existingEvent;
        this.onSuccess = onSuccess;
        this.sectionRows = new ArrayList<>();
        
        setupUI();
        loadData();
    }
    
    private void setupUI() {
        setSpacing(0);
        setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        VBox header = createHeader();
        
        // Scrollable Content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f5f5f5;");
        
        // Event Details Section
        VBox eventDetailsCard = createEventDetailsCard();
        
        // Section Configuration Section
        VBox sectionConfigCard = createSectionConfigCard();
        
        content.getChildren().addAll(eventDetailsCard, sectionConfigCard);
        scrollPane.setContent(content);
        
        // Footer with buttons
        HBox footer = createFooter();
        
        getChildren().addAll(header, scrollPane, footer);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        Label titleLabel = new Label(existingEvent == null ? "Create New Event" : "Edit Event");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        Label subtitleLabel = new Label(existingEvent == null ? 
            "Fill in event details and configure section pricing" : 
            "Update event information");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createEventDetailsCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        // Section Title
        Label sectionTitle = new Label("Event Details");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        Separator separator = new Separator();
        
        // Form Grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));
        
        // Event Name
        Label nameLabel = new Label("Event Name *");
        nameLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        nameField = new TextField();
        nameField.setPromptText("e.g., Championship Final 2025");
        nameField.setStyle("-fx-pref-height: 35; -fx-font-size: 13;");
        
        // Event Type
        Label typeLabel = new Label("Event Type *");
        typeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Football Match", "Concert", "Other Event");
        typeCombo.setPromptText("Select event type");
        typeCombo.setStyle("-fx-pref-height: 35; -fx-font-size: 13;");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setOnAction(e -> loadSections());
        
        // Date
        Label dateLabel = new Label("Event Date *");
        dateLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        datePicker = new DatePicker();
        datePicker.setPromptText("Select date");
        datePicker.setStyle("-fx-pref-height: 35; -fx-font-size: 13;");
        datePicker.setMaxWidth(Double.MAX_VALUE);
        
        // Time
        Label timeLabel = new Label("Event Time *");
        timeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        timeField = new TextField();
        timeField.setPromptText("HH:mm (e.g., 19:30)");
        timeField.setStyle("-fx-pref-height: 35; -fx-font-size: 13;");
        
        // Status
        Label statusLabel = new Label("Status");
        statusLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        statusCombo = new ComboBox<>();
        // Database constraint requires: 'Active', 'Cancelled', 'Completed'
        statusCombo.getItems().addAll("Active", "Cancelled", "Completed");
        statusCombo.setValue("Active");
        statusCombo.setStyle("-fx-pref-height: 35; -fx-font-size: 13;");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        
        // Description
        Label descLabel = new Label("Description");
        descLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        descArea = new TextArea();
        descArea.setPromptText("Event description (optional)");
        descArea.setPrefRowCount(3);
        descArea.setStyle("-fx-font-size: 13;");
        
        // Add to grid (2 columns)
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 0, 1);
        grid.add(typeLabel, 1, 0);
        grid.add(typeCombo, 1, 1);
        
        grid.add(dateLabel, 0, 2);
        grid.add(datePicker, 0, 3);
        grid.add(timeLabel, 1, 2);
        grid.add(timeField, 1, 3);
        
        grid.add(statusLabel, 0, 4);
        grid.add(statusCombo, 0, 5);
        
        grid.add(descLabel, 0, 6, 2, 1);
        grid.add(descArea, 0, 7, 2, 1);
        
        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);
        
        card.getChildren().addAll(sectionTitle, separator, grid);
        return card;
    }
    
    private VBox createSectionConfigCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        // Section Title with Total Seats
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionTitle = new Label("Section Configuration");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        totalSeatsLabel = new Label("Total Seats: 0");
        totalSeatsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        totalSeatsLabel.setTextFill(Color.web("#3498db"));
        totalSeatsLabel.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 8 15; -fx-background-radius: 5;");
        
        titleRow.getChildren().addAll(sectionTitle, spacer, totalSeatsLabel);
        
        Label instructionLabel = new Label("Configure capacity and pricing for each section. Total must not exceed maximum capacity.");
        instructionLabel.setFont(Font.font("System", 13));
        instructionLabel.setTextFill(Color.web("#7f8c8d"));
        
        Separator separator = new Separator();
        
        // Sections Container
        sectionsContainer = new VBox(10);
        sectionsContainer.setPadding(new Insets(10, 0, 0, 0));
        
        card.getChildren().addAll(titleRow, instructionLabel, separator, sectionsContainer);
        return card;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(20, 30, 20, 30));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        
        errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#e74c3c"));
        errorLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        errorLabel.setVisible(false);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14; " +
                              "-fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> onSuccess.run());
        
        saveButton = new Button(existingEvent == null ? "Create Event" : "Update Event");
        saveButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14; " +
                           "-fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> handleSave());
        
        // Hover effects
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(cancelButton.getStyle() + "-fx-background-color: #7f8c8d;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(cancelButton.getStyle().replace("-fx-background-color: #7f8c8d;", "")));
        
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(saveButton.getStyle() + "-fx-background-color: #2980b9;"));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(saveButton.getStyle().replace("-fx-background-color: #2980b9;", "")));
        
        footer.getChildren().addAll(errorLabel, spacer, cancelButton, saveButton);
        return footer;
    }
    
    private void loadData() {
        if (existingEvent != null) {
            // Load existing event data
            nameField.setText(existingEvent.getEventName());
            typeCombo.setValue(existingEvent.getEventType());
            datePicker.setValue(existingEvent.getEventDate());
            timeField.setText(existingEvent.getEventTime().toString());
            descArea.setText(existingEvent.getDescription());
            statusCombo.setValue(existingEvent.getStatus());
            
            // Load existing sections
            loadSections();
        }
    }
    
    private void loadSections() {
        sectionsContainer.getChildren().clear();
        sectionRows.clear();
        
        String eventType = typeCombo.getValue();
        if (eventType == null) {
            return;
        }
        
        // Calculate max capacity from stadium sections
        maxCapacity = sectionService.getTotalCapacityForEventType(eventType);
        
        // Get appropriate sections based on event type
        List<Section> sections;
        if (eventType.toUpperCase().contains("FOOTBALL")) {
            sections = sectionService.getTribuneSections();  // Football uses tribune sections only
        } else {
            sections = sectionService.getAllSections();      // Concert uses ALL sections (tribune + field)
        }
        
        if (sections.isEmpty()) {
            Label noSectionsLabel = new Label("No sections available for this event type");
            noSectionsLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14;");
            sectionsContainer.getChildren().add(noSectionsLabel);
            return;
        }
        
        // Load existing event sections if editing
        List<EventSection> existingEventSections = new ArrayList<>();
        if (existingEvent != null) {
            existingEventSections = eventSectionService.getEventSections(existingEvent.getId());
        }
        
        // Create header row
        HBox headerRow = new HBox(10);
        headerRow.setPadding(new Insets(5, 0, 10, 0));
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionNameHeader = new Label("Section");
        sectionNameHeader.setFont(Font.font("System", FontWeight.BOLD, 13));
        sectionNameHeader.setPrefWidth(200);
        
        Label capacityHeader = new Label("Capacity (Seats)");
        capacityHeader.setFont(Font.font("System", FontWeight.BOLD, 13));
        capacityHeader.setPrefWidth(150);
        
        Label priceHeader = new Label("Price ($)");
        priceHeader.setFont(Font.font("System", FontWeight.BOLD, 13));
        priceHeader.setPrefWidth(150);
        
        Label enabledHeader = new Label("Enabled");
        enabledHeader.setFont(Font.font("System", FontWeight.BOLD, 13));
        enabledHeader.setPrefWidth(80);
        
        headerRow.getChildren().addAll(sectionNameHeader, capacityHeader, priceHeader, enabledHeader);
        sectionsContainer.getChildren().add(headerRow);
        
        // Create rows for each section
        for (Section section : sections) {
            SectionRow row = new SectionRow(section);
            
            // Load existing data if editing
            for (EventSection es : existingEventSections) {
                if (es.getSectionId() == section.getSectionId()) {
                    row.setData(es.getTotalCapacity(), es.getPrice(), true);
                    break;
                }
            }
            
            sectionRows.add(row);
            sectionsContainer.getChildren().add(row);
        }
        
        updateTotalSeats();
    }
    
    private void updateTotalSeats() {
        int total = 0;
        for (SectionRow row : sectionRows) {
            if (row.isEnabled()) {
                total += row.getCapacity();
            }
        }
        
        // Check if exceeds max capacity (from stadium configuration)
        if (maxCapacity > 0) {
            if (total > maxCapacity) {
                // Show warning in red
                totalSeatsLabel.setText("Total Seats: " + total + " ⚠ EXCEEDS MAX!");
                totalSeatsLabel.setTextFill(Color.web("#e74c3c"));
                totalSeatsLabel.setStyle("-fx-background-color: #fadbd8; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;");
            } else {
                // Show normal
                totalSeatsLabel.setText("Total Seats: " + total + " / " + maxCapacity);
                totalSeatsLabel.setTextFill(Color.web("#27ae60"));
                totalSeatsLabel.setStyle("-fx-background-color: #d5f4e6; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;");
            }
        } else {
            totalSeatsLabel.setText("Total Seats: " + total);
            totalSeatsLabel.setTextFill(Color.web("#3498db"));
            totalSeatsLabel.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        }
    }
    
    private void handleSave() {
        errorLabel.setVisible(false);
        
        // Validate event details
        if (nameField.getText().trim().isEmpty()) {
            showError("Event name is required");
            return;
        }
        
        if (typeCombo.getValue() == null) {
            showError("Event type is required");
            return;
        }
        
        if (datePicker.getValue() == null) {
            showError("Event date is required");
            return;
        }
        
        if (timeField.getText().trim().isEmpty()) {
            showError("Event time is required");
            return;
        }
        
        LocalTime time;
        try {
            time = LocalTime.parse(timeField.getText().trim());
        } catch (Exception e) {
            showError("Invalid time format. Use HH:mm (e.g., 19:30)");
            return;
        }
        
        // Check if maxCapacity was calculated (event type selected and sections loaded)
        if (maxCapacity <= 0) {
            showError("Please select an event type to load stadium sections");
            return;
        }
        
        // Calculate total seats
        final int totalSeats;
        {
            int temp = 0;
            for (SectionRow row : sectionRows) {
                if (row.isEnabled()) {
                    temp += row.getCapacity();
                }
            }
            totalSeats = temp;
        }
        
        List<SectionRow> enabledSections = new ArrayList<>();
        for (SectionRow row : sectionRows) {
            if (row.isEnabled()) {
                if (row.getCapacity() <= 0) {
                    showError("Capacity must be greater than 0 for enabled sections");
                    return;
                }
                if (row.getPrice() <= 0) {
                    showError("Price must be greater than 0 for enabled sections");
                    return;
                }
                // Validate section capacity doesn't exceed its physical maximum
                // Skip this check for field sections (which have 0 fixed capacity)
                if (row.getSection().getTotalCapacity() > 0 && 
                    row.getCapacity() > row.getSection().getTotalCapacity()) {
                    showError("Section '" + row.getSection().getSectionName() + "' capacity (" + 
                             row.getCapacity() + " seats) exceeds its maximum capacity (" + 
                             row.getSection().getTotalCapacity() + " seats)!");
                    return;
                }
                enabledSections.add(row);
            }
        }
        
        if (totalSeats == 0) {
            showError("At least one section must be enabled with capacity");
            return;
        }
        
        // Validate total doesn't exceed max capacity
        if (totalSeats > maxCapacity) {
            showError("Total section capacity (" + totalSeats + " seats) exceeds maximum capacity (" + maxCapacity + " seats)!\n" +
                     "Please reduce section capacities or increase maximum capacity.");
            return;
        }
        
        // Disable save button
        saveButton.setDisable(true);
        saveButton.setText("Saving...");
        
        // Save in background thread
        new Thread(() -> {
            try {
                boolean success;
                Event event;
                
                // Map combo box value to database event type
                String eventType = typeCombo.getValue().toUpperCase().contains("FOOTBALL") ? "Football" : "Concert";
                
                if (existingEvent == null) {
                    // Create new event
                    event = new Event(
                        nameField.getText().trim(),
                        eventType,
                        datePicker.getValue(),
                        time,
                        descArea.getText().trim(),
                        totalSeats
                    );
                    event.setStatus(statusCombo.getValue());
                    success = eventService.createEvent(event);
                } else {
                    // Update existing event
                    event = existingEvent;
                    event.setEventName(nameField.getText().trim());
                    event.setEventType(eventType);
                    event.setEventDate(datePicker.getValue());
                    event.setEventTime(time);
                    event.setDescription(descArea.getText().trim());
                    event.setTotalSeats(totalSeats);
                    event.setStatus(statusCombo.getValue());
                    success = eventService.updateEvent(event);
                }
                
                if (success && event.getId() > 0) {
                    // If updating, delete old sections first
                    if (existingEvent != null) {
                        List<EventSection> oldSections = eventSectionService.getEventSections(event.getId());
                        for (EventSection oldSection : oldSections) {
                            eventSectionService.deleteEventSection(oldSection.getEventSectionId());
                        }
                    }
                    
                    // Save new sections
                    for (SectionRow row : enabledSections) {
                        EventSection es = new EventSection();
                        es.setEventId(event.getId());
                        es.setSectionId(row.getSection().getSectionId());
                        es.setTotalCapacity(row.getCapacity());
                        es.setAvailableSeats(row.getCapacity());  // Set available = total capacity
                        es.setPrice(row.getPrice());
                        es.setSectionTitle(row.getSection().getSectionName());
                        
                        System.out.println("DEBUG: Creating EventSection - " +
                                         "SectionID=" + row.getSection().getSectionId() +
                                         ", Title=" + row.getSection().getSectionName() +
                                         ", TotalCapacity=" + row.getCapacity() +
                                         ", AvailableSeats=" + row.getCapacity() +
                                         ", Price=" + row.getPrice());
                        
                        eventSectionService.createEventSection(es);
                    }
                    
                    // Success - return to UI thread
                    javafx.application.Platform.runLater(() -> {
                        showSuccess("Event " + (existingEvent == null ? "created" : "updated") + " successfully!");
                        onSuccess.run();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showError("Failed to save event. Please try again.");
                        saveButton.setDisable(false);
                        saveButton.setText(existingEvent == null ? "Create Event" : "Update Event");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showError("Error: " + e.getMessage());
                    saveButton.setDisable(false);
                    saveButton.setText(existingEvent == null ? "Create Event" : "Update Event");
                });
            }
        }).start();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Inner class for section row
     */
    private class SectionRow extends HBox {
        private final Section section;
        private final TextField capacityField;
        private final TextField priceField;
        private final CheckBox enabledCheck;
        
        public SectionRow(Section section) {
            this.section = section;
            setSpacing(10);
            setPadding(new Insets(5));
            setAlignment(Pos.CENTER_LEFT);
            setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
            
            // Section name with max capacity
            Label nameLabel = new Label(section.getSectionName() + " (Max: " + section.getTotalCapacity() + ")");
            nameLabel.setFont(Font.font("System", 13));
            nameLabel.setPrefWidth(200);
            
            // Capacity field
            capacityField = new TextField();
            capacityField.setPromptText("Max " + section.getTotalCapacity());
            capacityField.setPrefWidth(150);
            capacityField.setStyle("-fx-pref-height: 32; -fx-font-size: 12;");
            capacityField.textProperty().addListener((obs, old, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    capacityField.setText(old);
                } else {
                    // Validate against section's max capacity
                    if (!newVal.isEmpty()) {
                        try {
                            int enteredCapacity = Integer.parseInt(newVal);
                            // Skip validation for field sections (totalCapacity = 0)
                            // They can have any capacity for the event
                            if (section.getTotalCapacity() > 0 && enteredCapacity > section.getTotalCapacity()) {
                                // Show error styling (only for tribune sections)
                                capacityField.setStyle("-fx-pref-height: 32; -fx-font-size: 12; -fx-border-color: #e74c3c; -fx-border-width: 2;");
                            } else {
                                // Normal styling
                                capacityField.setStyle("-fx-pref-height: 32; -fx-font-size: 12; -fx-border-color: #4CAF50; -fx-border-width: 1;");
                            }
                        } catch (NumberFormatException e) {
                            capacityField.setStyle("-fx-pref-height: 32; -fx-font-size: 12;");
                        }
                    } else {
                        capacityField.setStyle("-fx-pref-height: 32; -fx-font-size: 12;");
                    }
                    updateTotalSeats();
                }
            });
            
            // Price field
            priceField = new TextField();
            priceField.setPromptText("Price");
            priceField.setPrefWidth(150);
            priceField.setStyle("-fx-pref-height: 32; -fx-font-size: 12;");
            priceField.textProperty().addListener((obs, old, newVal) -> {
                if (!newVal.matches("\\d*\\.?\\d*")) {
                    priceField.setText(old);
                }
            });
            
            // Enabled checkbox
            enabledCheck = new CheckBox();
            enabledCheck.setSelected(false);
            enabledCheck.setPrefWidth(80);
            enabledCheck.setOnAction(e -> {
                capacityField.setDisable(!enabledCheck.isSelected());
                priceField.setDisable(!enabledCheck.isSelected());
                updateTotalSeats();
            });
            
            capacityField.setDisable(true);
            priceField.setDisable(true);
            
            getChildren().addAll(nameLabel, capacityField, priceField, enabledCheck);
        }
        
        public Section getSection() {
            return section;
        }
        
        public int getCapacity() {
            try {
                return Integer.parseInt(capacityField.getText().trim());
            } catch (Exception e) {
                return 0;
            }
        }
        
        public double getPrice() {
            try {
                return Double.parseDouble(priceField.getText().trim());
            } catch (Exception e) {
                return 0.0;
            }
        }
        
        public boolean isEnabled() {
            return enabledCheck.isSelected();
        }
        
        public void setData(int capacity, double price, boolean enabled) {
            capacityField.setText(String.valueOf(capacity));
            priceField.setText(String.valueOf(price));
            enabledCheck.setSelected(enabled);
            capacityField.setDisable(!enabled);
            priceField.setDisable(!enabled);
        }
    }
}
