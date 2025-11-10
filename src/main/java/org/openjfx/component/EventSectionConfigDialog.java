package org.openjfx.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.openjfx.model.EventSection;
import org.openjfx.model.Section;
import org.openjfx.service.EventSectionService;
import org.openjfx.service.SectionService;
import org.openjfx.util.IconUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for admins to configure section titles and pricing for an event
 * Used when creating or editing events
 */
public class EventSectionConfigDialog extends Stage {
    
    private final int eventId;
    private final String eventType; // FOOTBALL or CONCERT
    private int eventTotalSeats; // Maximum capacity for the event
    
    private VBox sectionsContainer;
    private List<SectionConfigRow> configRows;
    private Button saveButton;
    
    private final SectionService sectionService;
    private final EventSectionService eventSectionService;
    
    private Runnable onSaveComplete;
    
    public EventSectionConfigDialog(int eventId, String eventType, int totalSeats) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventTotalSeats = totalSeats; // Use passed value instead of database call
        this.configRows = new ArrayList<>();
        this.sectionService = new SectionService();
        this.eventSectionService = new EventSectionService();
        
        initializeDialog();
        initializeUI();
        loadSections();
    }
    
    private void initializeDialog() {
        setTitle("Configure Event Sections - Pricing & Titles");
        initModality(Modality.APPLICATION_MODAL);
        setWidth(800);
        setHeight(600);
        setResizable(false);
    }
    
    private void initializeUI() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        VBox header = createHeader();
        
        // Content area with scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        sectionsContainer = new VBox(15);
        sectionsContainer.setPadding(new Insets(20));
        sectionsContainer.setStyle("-fx-background-color: white;");
        
        scrollPane.setContent(sectionsContainer);
        
        // Footer with buttons
        HBox footer = createFooter();
        
        root.getChildren().addAll(header, scrollPane, footer);
        
        Scene scene = new Scene(root);
        setScene(scene);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2196F3;");
        
        Label titleLabel = new Label("Configure Event Sections");
        titleLabel.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        
        Label subtitleLabel = new Label(
            "Set custom titles and pricing for each section. " +
            (eventType.equals("CONCERT") ? "Concert events include field zones." : "")
        );
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: rgba(255,255,255,0.9);"
        );
        
        // Add capacity info label
        Label capacityInfoLabel = new Label("Event Maximum Capacity: " + eventTotalSeats + " seats");
        capacityInfoLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #FFEB3B;" +
            "-fx-background-color: rgba(0,0,0,0.2);" +
            "-fx-padding: 8px 12px;" +
            "-fx-background-radius: 4px;"
        );
        
        header.getChildren().addAll(titleLabel, subtitleLabel, capacityInfoLabel);
        return header;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 20, 15, 20));
        footer.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle(
            "-fx-background-color: #6c757d;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        cancelButton.setOnAction(e -> close());
        
        saveButton = new Button("Save Configuration");
        saveButton.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 30px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        saveButton.setOnAction(e -> saveConfiguration());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        footer.getChildren().addAll(cancelButton, spacer, saveButton);
        return footer;
    }
    
    private void loadSections() {
        new Thread(() -> {
            try {
                List<Section> sections;
                
                // Load sections based on event type
                if (eventType.equals("CONCERT")) {
                    // Load all sections (tribunes + field zones)
                    sections = sectionService.getAllSections();
                } else {
                    // Load only tribune sections for football
                    sections = sectionService.getTribuneSections();
                }
                
                Platform.runLater(() -> {
                    populateSections(sections);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Failed to load sections");
                });
            }
        }).start();
    }
    
    private void populateSections(List<Section> sections) {
        sectionsContainer.getChildren().clear();
        configRows.clear();
        
        // Info box
        HBox infoBox = new HBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle(
            "-fx-background-color: #e3f2fd;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #2196F3;" +
            "-fx-border-radius: 5px;" +
            "-fx-border-width: 1px;"
        );
        
        Label infoIcon = new Label();
        infoIcon.setGraphic(IconUtil.createIcon(FontAwesomeIcon.INFO_CIRCLE, 16));
        infoIcon.setStyle("-fx-text-fill: #2196F3;");
        
        Label infoText = new Label(
            "Set a custom title (e.g., VIP, Standard, Premium) and price for each section."
        );
        infoText.setWrapText(true);
        infoText.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #1976D2;"
        );
        
        infoBox.getChildren().addAll(infoIcon, infoText);
        sectionsContainer.getChildren().add(infoBox);
        
        // Add separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        sectionsContainer.getChildren().add(separator);
        
        // Section config rows
        for (Section section : sections) {
            SectionConfigRow configRow = new SectionConfigRow(section);
            configRows.add(configRow);
            sectionsContainer.getChildren().add(configRow);
        }
    }
    
    private void saveConfiguration() {
        System.out.println("DEBUG: saveConfiguration called");
        
        // First, identify which rows have been filled in (sections user wants to include)
        List<SectionConfigRow> configuredRows = new ArrayList<>();
        List<SectionConfigRow> emptyRows = new ArrayList<>();
        
        for (SectionConfigRow row : configRows) {
            String title = row.titleField.getText().trim();
            String price = row.priceField.getText().trim();
            
            // If both fields are empty, skip this section (user doesn't want it)
            if (title.isEmpty() && price.isEmpty()) {
                emptyRows.add(row);
                continue;
            }
            
            // If only one field is filled, it's an error
            if (title.isEmpty() || price.isEmpty()) {
                row.errorLabel.setText("Both title and price must be filled, or leave both empty to skip");
                row.errorLabel.setVisible(true);
                return;
            }
            
            // Validate this row
            if (!row.validate()) {
                return;
            }
            
            configuredRows.add(row);
        }
        
        System.out.println("DEBUG: Configured sections: " + configuredRows.size());
        
        // Check if at least one section is configured
        if (configuredRows.isEmpty()) {
            showError("Please configure at least one section for this event.");
            return;
        }
        
        // Calculate total capacity ONLY for configured sections
        int totalCapacity = 0;
        for (SectionConfigRow row : configuredRows) {
            totalCapacity += row.getTotalCapacity();
        }
        
        System.out.println("DEBUG: Total capacity: " + totalCapacity + ", Event max: " + eventTotalSeats);
        
        // Validate against event's maximum capacity
        if (totalCapacity > eventTotalSeats) {
            System.out.println("DEBUG: CAPACITY EXCEEDED! Showing error");
            showError(
                "Total section capacity exceeds event maximum!\n\n" +
                "Event Maximum: " + eventTotalSeats + " seats\n" +
                "Configured Sections Total: " + totalCapacity + " seats\n" +
                "Excess: " + (totalCapacity - eventTotalSeats) + " seats\n\n" +
                "Please:\n" +
                "- Remove some sections (leave title and price empty), OR\n" +
                "- Increase the event's total seats capacity"
            );
            return;
        }
        
        saveButton.setDisable(true);
        saveButton.setText("Saving...");
        
        new Thread(() -> {
            try {
                boolean allSuccess = true;
                
                // Save ONLY configured sections
                for (SectionConfigRow row : configuredRows) {
                    EventSection eventSection = new EventSection();
                    eventSection.setEventId(eventId);
                    eventSection.setSectionId(row.getSection().getSectionId());
                    eventSection.setSectionTitle(row.getSectionTitle());
                    eventSection.setPrice(row.getPrice());
                    eventSection.setTotalCapacity(row.getTotalCapacity());
                    eventSection.setAvailableSeats(row.getTotalCapacity());
                    
                    System.out.println("DEBUG: Creating EventSection - " +
                                     "SectionID=" + row.getSection().getSectionId() +
                                     ", Title=" + row.getSectionTitle() +
                                     ", TotalCapacity=" + row.getTotalCapacity() +
                                     ", AvailableSeats=" + row.getTotalCapacity());
                    
                    boolean success = eventSectionService.createEventSection(eventSection);
                    if (!success) {
                        allSuccess = false;
                        break;
                    }
                }
                
                final boolean finalSuccess = allSuccess;
                Platform.runLater(() -> {
                    if (finalSuccess) {
                        showSuccess();
                        if (onSaveComplete != null) {
                            onSaveComplete.run();
                        }
                        close();
                    } else {
                        showError("Failed to save some configurations. Please try again.");
                        saveButton.setDisable(false);
                        saveButton.setText("Save Configuration");
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("An error occurred: " + e.getMessage());
                    saveButton.setDisable(false);
                    saveButton.setText("Save Configuration");
                });
            }
        }).start();
    }
    
    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Configuration Saved");
        alert.setContentText("Event sections have been configured successfully!");
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Configuration Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setOnSaveComplete(Runnable callback) {
        this.onSaveComplete = callback;
    }
    
    /**
     * Row component for configuring a single section
     */
    private static class SectionConfigRow extends VBox {
        
        private final Section section;
        private TextField titleField;
        private TextField priceField;
        private TextField capacityField; // For FIELD sections
        private Label errorLabel;
        
        public SectionConfigRow(Section section) {
            this.section = section;
            initializeUI();
        }
        
        private void initializeUI() {
            setSpacing(12);
            setPadding(new Insets(15));
            setStyle(
                "-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: #dee2e6;" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
            );
            
            // Section header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label icon = new Label();
            icon.setGraphic(IconUtil.createIcon(
                section.isTribune() ? FontAwesomeIcon.TH : FontAwesomeIcon.MAP_MARKER, 
                16
            ));
            icon.setStyle("-fx-text-fill: " + (section.isTribune() ? "#2196F3" : "#4CAF50") + ";");
            
            Label nameLabel = new Label(section.getSectionName());
            nameLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2c3e50;"
            );
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label typeLabel = new Label(section.getSectionType());
            typeLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: " + (section.isTribune() ? "#2196F3" : "#4CAF50") + ";" +
                "-fx-padding: 4px 10px;" +
                "-fx-background-radius: 12px;"
            );
            
            // Show capacity - if 0, show as "Variable" for field sections
            String capacityText = section.getTotalCapacity() > 0 
                ? section.getTotalCapacity() + " seats" 
                : "Standing area";
            Label capacityLabel = new Label(capacityText);
            capacityLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7f8c8d;"
            );
            
            header.getChildren().addAll(icon, nameLabel, spacer, typeLabel, capacityLabel);
            
            // Input fields
            GridPane inputGrid = new GridPane();
            inputGrid.setHgap(15);
            inputGrid.setVgap(10);
            inputGrid.setPadding(new Insets(10, 0, 0, 0));
            
            // Title field
            Label titleLabel = new Label("Section Title:");
            titleLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #495057;"
            );
            
            titleField = new TextField();
            titleField.setPromptText("e.g., VIP, Standard, Premium");
            titleField.setPrefWidth(200);
            titleField.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-padding: 8px 12px;" +
                "-fx-background-radius: 5px;" +
                "-fx-border-color: #ced4da;" +
                "-fx-border-radius: 5px;" +
                "-fx-border-width: 1px;"
            );
            
            // Price field
            Label priceLabel = new Label("Price (Rp):");
            priceLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #495057;"
            );
            
            priceField = new TextField();
            priceField.setPromptText("e.g., 150000");
            priceField.setPrefWidth(150);
            priceField.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-padding: 8px 12px;" +
                "-fx-background-radius: 5px;" +
                "-fx-border-color: #ced4da;" +
                "-fx-border-radius: 5px;" +
                "-fx-border-width: 1px;"
            );
            
            // Only allow numbers in price field
            priceField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    priceField.setText(newVal.replaceAll("[^\\d]", ""));
                }
            });
            
            inputGrid.add(titleLabel, 0, 0);
            inputGrid.add(titleField, 1, 0);
            inputGrid.add(priceLabel, 2, 0);
            inputGrid.add(priceField, 3, 0);
            
            // For FIELD sections (standing area), add capacity field
            if (section.isField() || section.getTotalCapacity() == 0) {
                Label capacityInputLabel = new Label("Capacity:");
                capacityInputLabel.setStyle(
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #495057;"
                );
                
                capacityField = new TextField();
                capacityField.setPromptText("e.g., 1000");
                capacityField.setPrefWidth(120);
                capacityField.setStyle(
                    "-fx-font-size: 13px;" +
                    "-fx-padding: 8px 12px;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-border-color: #ced4da;" +
                    "-fx-border-radius: 5px;" +
                    "-fx-border-width: 1px;"
                );
                
                // Only allow numbers
                capacityField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.matches("\\d*")) {
                        capacityField.setText(newVal.replaceAll("[^\\d]", ""));
                    }
                });
                
                inputGrid.add(capacityInputLabel, 0, 1);
                inputGrid.add(capacityField, 1, 1);
                
                Label infoLabel = new Label("Set max capacity for this standing area");
                infoLabel.setStyle(
                    "-fx-font-size: 10px;" +
                    "-fx-text-fill: #6c757d;" +
                    "-fx-font-style: italic;"
                );
                inputGrid.add(infoLabel, 2, 1, 2, 1);
            }
            
            // Error label
            errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 11px;");
            errorLabel.setVisible(false);
            
            getChildren().addAll(header, inputGrid, errorLabel);
        }
        
        public boolean validate() {
            errorLabel.setVisible(false);
            
            String title = titleField.getText().trim();
            String priceText = priceField.getText().trim();
            
            if (title.isEmpty()) {
                errorLabel.setText("Section title is required");
                errorLabel.setVisible(true);
                return false;
            }
            
            if (title.length() < 3) {
                errorLabel.setText("Section title must be at least 3 characters");
                errorLabel.setVisible(true);
                return false;
            }
            
            if (priceText.isEmpty()) {
                errorLabel.setText("Price is required");
                errorLabel.setVisible(true);
                return false;
            }
            
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    errorLabel.setText("Price must be greater than 0");
                    errorLabel.setVisible(true);
                    return false;
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("Invalid price format");
                errorLabel.setVisible(true);
                return false;
            }
            
            // Validate capacity for FIELD sections
            if (capacityField != null) {
                String capacityText = capacityField.getText().trim();
                if (capacityText.isEmpty()) {
                    errorLabel.setText("Capacity is required for standing areas");
                    errorLabel.setVisible(true);
                    return false;
                }
                
                try {
                    int capacity = Integer.parseInt(capacityText);
                    if (capacity <= 0) {
                        errorLabel.setText("Capacity must be greater than 0");
                        errorLabel.setVisible(true);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    errorLabel.setText("Invalid capacity format");
                    errorLabel.setVisible(true);
                    return false;
                }
            }
            
            return true;
        }
        
        public Section getSection() {
            return section;
        }
        
        public String getSectionTitle() {
            return titleField.getText().trim();
        }
        
        public double getPrice() {
            return Double.parseDouble(priceField.getText().trim());
        }
        
        public int getTotalCapacity() {
            // For FIELD sections, use the entered capacity
            if (capacityField != null && !capacityField.getText().trim().isEmpty()) {
                return Integer.parseInt(capacityField.getText().trim());
            }
            // For TRIBUNE sections, use the section's calculated capacity
            return section.getTotalCapacity();
        }
    }
}
