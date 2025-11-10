package org.openjfx.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.openjfx.model.Section;
import org.openjfx.service.SectionService;
import org.openjfx.service.SeatGenerationService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.sql.SQLException;
import java.util.List;

/**
 * Stadium Configuration View
 * Displays stadium sections, capacities, and configuration for different event types
 */
public class StadiumConfigView extends VBox {
    
    private final SectionService sectionService;
    private VBox tribunesContainer;
    private VBox fieldZonesContainer;
    private Label totalCapacityLabel;
    private Label tribuneCapacityLabel;
    private Label fieldCapacityLabel;
    
    public StadiumConfigView() {
        this.sectionService = new SectionService();
        
        initializeUI();
        loadStadiumData();
    }
    
    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #ecf0f1;");
        
        // Header
        HBox header = createHeader();
        
        // Summary cards
        HBox summaryCards = createSummaryCards();
        
        // Configuration sections
        VBox configSection = createConfigSection();
        
        getChildren().addAll(header, summaryCards, configSection);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.BUILDING);
        icon.setSize("28");
        icon.setFill(javafx.scene.paint.Color.web("#2c3e50"));
        
        Label titleLabel = new Label("Stadium Configuration");
        titleLabel.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Label subtitleLabel = new Label("Manage stadium sections, capacities, and event configurations");
        subtitleLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        VBox titleBox = new VBox(5);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        
        header.getChildren().addAll(icon, titleBox);
        return header;
    }
    
    private HBox createSummaryCards() {
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER);
        
        // Total Capacity Card
        VBox totalCard = createCapacityCard(
            "Total Stadium Capacity",
            "0",
            "#3498db",
            FontAwesomeIcon.BUILDING
        );
        totalCapacityLabel = (Label) totalCard.lookup("#capacityValue");
        
        // Tribune Capacity Card
        VBox tribuneCard = createCapacityCard(
            "Tribune Sections",
            "0",
            "#e74c3c",
            FontAwesomeIcon.USERS
        );
        tribuneCapacityLabel = (Label) tribuneCard.lookup("#capacityValue");
        
        // Field Capacity Card
        VBox fieldCard = createCapacityCard(
            "Field Zones (Concert)",
            "0",
            "#27ae60",
            FontAwesomeIcon.MUSIC
        );
        fieldCapacityLabel = (Label) fieldCard.lookup("#capacityValue");
        
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(tribuneCard, Priority.ALWAYS);
        HBox.setHgrow(fieldCard, Priority.ALWAYS);
        
        cards.getChildren().addAll(totalCard, tribuneCard, fieldCard);
        return cards;
    }
    
    private VBox createCapacityCard(String title, String value, String color, FontAwesomeIcon iconType) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("32");
        icon.setFill(javafx.scene.paint.Color.web(color));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        Label valueLabel = new Label(value);
        valueLabel.setId("capacityValue");
        valueLabel.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + color + ";"
        );
        
        Label seatsLabel = new Label("seats");
        seatsLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #95a5a6;"
        );
        
        card.getChildren().addAll(icon, titleLabel, valueLabel, seatsLabel);
        return card;
    }
    
    private VBox createConfigSection() {
        VBox section = new VBox(20);
        
        // Tribune Sections
        VBox tribuneSection = createSectionGroup(
            "Tribune Sections",
            "Available for: Football Matches & Concerts",
            "#e74c3c"
        );
        tribunesContainer = new VBox(15);
        tribuneSection.getChildren().add(tribunesContainer);
        
        // Field Zones
        VBox fieldSection = createSectionGroup(
            "Field Zones",
            "Available for: Concerts Only (Standing Area)",
            "#27ae60"
        );
        fieldZonesContainer = new VBox(15);
        fieldSection.getChildren().add(fieldZonesContainer);
        
        section.getChildren().addAll(tribuneSection, fieldSection);
        return section;
    }
    
    private VBox createSectionGroup(String title, String subtitle, String color) {
        VBox group = new VBox(15);
        group.setPadding(new Insets(20));
        group.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + color + ";"
        );
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        group.getChildren().addAll(titleLabel, subtitleLabel);
        return group;
    }
    
    private void loadStadiumData() {
        new Thread(() -> {
            try {
                List<Section> sections = sectionService.getAllSections();
                
                Platform.runLater(() -> {
                    displaySections(sections);
                    updateSummary(sections);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Failed to load stadium data: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void displaySections(List<Section> sections) {
        tribunesContainer.getChildren().clear();
        fieldZonesContainer.getChildren().clear();
        
        for (Section section : sections) {
            SectionCard card = new SectionCard(section);
            
            if (section.isTribune()) {
                tribunesContainer.getChildren().add(card);
            } else {
                fieldZonesContainer.getChildren().add(card);
            }
        }
    }
    
    private void updateSummary(List<Section> sections) {
        int totalCapacity = 0;
        int tribuneCapacity = 0;
        int fieldCapacity = 0;
        
        for (Section section : sections) {
            int capacity = section.getTotalCapacity();
            totalCapacity += capacity;
            
            if (section.isTribune()) {
                tribuneCapacity += capacity;
            } else {
                fieldCapacity += capacity;
            }
        }
        
        totalCapacityLabel.setText(String.format("%,d", totalCapacity));
        tribuneCapacityLabel.setText(String.format("%,d", tribuneCapacity));
        fieldCapacityLabel.setText(String.format("%,d", fieldCapacity));
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to Load Stadium Data");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Inner class for section card display
     */
    private class SectionCard extends HBox {
        
        public SectionCard(Section section) {
            setSpacing(20);
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(15));
            setStyle(
                "-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: #dee2e6;" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
            );
            
            // Icon
            FontAwesomeIconView icon = new FontAwesomeIconView(
                section.isTribune() ? FontAwesomeIcon.USERS : FontAwesomeIcon.MUSIC
            );
            icon.setSize("24");
            icon.setFill(javafx.scene.paint.Color.web(
                section.isTribune() ? "#e74c3c" : "#27ae60"
            ));
            
            // Section info
            VBox infoBox = new VBox(5);
            HBox.setHgrow(infoBox, Priority.ALWAYS);
            
            Label nameLabel = new Label(section.getSectionName());
            nameLabel.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2c3e50;"
            );
            
            String layout = section.isTribune() 
                ? String.format("%d rows × %d seats", section.getTotalRows(), section.getSeatsPerRow())
                : "Standing Area";
            
            Label layoutLabel = new Label(layout);
            layoutLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #7f8c8d;"
            );
            
            infoBox.getChildren().addAll(nameLabel, layoutLabel);
            
            // Type badge
            Label typeBadge = new Label(section.getSectionType());
            typeBadge.setPadding(new Insets(5, 15, 5, 15));
            typeBadge.setStyle(
                "-fx-background-color: " + (section.isTribune() ? "#fee" : "#efe") + ";" +
                "-fx-text-fill: " + (section.isTribune() ? "#e74c3c" : "#27ae60") + ";" +
                "-fx-background-radius: 15px;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
            );
            
            // Capacity
            VBox capacityBox = new VBox(3);
            capacityBox.setAlignment(Pos.CENTER);
            
            Label capacityValue = new Label(String.format("%,d", section.getTotalCapacity()));
            capacityValue.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2c3e50;"
            );
            
            Label capacityLabel = new Label("Total Seats");
            capacityLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #95a5a6;"
            );
            
            capacityBox.getChildren().addAll(capacityValue, capacityLabel);
            
            // Edit button
            Button editButton = new Button("Edit");
            editButton.setStyle(
                "-fx-background-color: #3498db;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 20;" +
                "-fx-background-radius: 5px;" +
                "-fx-cursor: hand;"
            );
            
            // Hover effect
            editButton.setOnMouseEntered(e -> 
                editButton.setStyle(
                    "-fx-background-color: #2980b9;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 8 20;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-cursor: hand;"
                )
            );
            
            editButton.setOnMouseExited(e -> 
                editButton.setStyle(
                    "-fx-background-color: #3498db;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 8 20;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-cursor: hand;"
                )
            );
            
            editButton.setOnAction(e -> handleEditSection(section));
            
            // Sync Seats button (only for TRIBUNE sections)
            Button syncButton = null;
            if (section.isTribune()) {
                syncButton = new Button("Sync Seats");
                syncButton.setStyle(
                    "-fx-background-color: #9b59b6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 8 20;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-cursor: hand;"
                );
                
                // Hover effect
                Button finalSyncButton = syncButton;
                syncButton.setOnMouseEntered(e -> 
                    finalSyncButton.setStyle(
                        "-fx-background-color: #8e44ad;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-cursor: hand;"
                    )
                );
                
                syncButton.setOnMouseExited(e -> 
                    finalSyncButton.setStyle(
                        "-fx-background-color: #9b59b6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-cursor: hand;"
                    )
                );
                
                syncButton.setOnAction(e -> handleSyncSeats(section));
            }
            
            if (syncButton != null) {
                getChildren().addAll(icon, infoBox, typeBadge, capacityBox, syncButton, editButton);
            } else {
                getChildren().addAll(icon, infoBox, typeBadge, capacityBox, editButton);
            }
        }
    }
    
    /**
     * Handle edit section button click
     */
    private void handleEditSection(Section section) {
        SectionFormDialog dialog = new SectionFormDialog(section);
        dialog.showAndWait().ifPresent(updatedSection -> {
            // Check if dimensions changed (rows or seatsPerRow)
            boolean dimensionsChanged = 
                (section.getTotalRows() != updatedSection.getTotalRows()) ||
                (section.getSeatsPerRow() != updatedSection.getSeatsPerRow());
            
            // Save to database
            new Thread(() -> {
                try {
                    boolean success = sectionService.updateSection(updatedSection);
                    
                    // If dimensions changed and it's a TRIBUNE section, regenerate seats
                    if (success && dimensionsChanged && "TRIBUNE".equals(updatedSection.getSectionType())) {
                        System.out.println("Section dimensions changed - regenerating seats...");
                        SeatGenerationService seatGenService = new SeatGenerationService();
                        seatGenService.regenerateSeatsForSection(updatedSection.getSectionId());
                    }
                    
                    Platform.runLater(() -> {
                        if (success) {
                            // Show success message
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText("Section Updated");
                            String message = "Section has been updated successfully!";
                            if (dimensionsChanged && "TRIBUNE".equals(updatedSection.getSectionType())) {
                                message += "\n\nSeats have been automatically regenerated to match the new configuration.";
                            }
                            alert.setContentText(message);
                            alert.showAndWait();
                            
                            // Reload data
                            loadStadiumData();
                        } else {
                            // Show error
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Update Failed");
                            alert.setContentText("Failed to update section. Please try again.");
                            alert.showAndWait();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Update Failed");
                        alert.setContentText("An error occurred: " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        });
    }
    
    /**
     * Handle sync seats button click
     */
    private void handleSyncSeats(Section section) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Sync Seats");
        confirmAlert.setHeaderText("Regenerate Seats for " + section.getSectionName() + "?");
        confirmAlert.setContentText(
            "This will:\n" +
            "• Delete all existing seat records for this section\n" +
            "• Generate new seats based on current configuration:\n" +
            "  " + section.getTotalRows() + " rows × " + section.getSeatsPerRow() + " seats = " + 
            section.getTotalCapacity() + " total seats\n\n" +
            "⚠️ Warning: Cannot proceed if there are existing bookings.\n\n" +
            "Continue?"
        );
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Run in background thread
                new Thread(() -> {
                    try {
                        SeatGenerationService seatGenService = new SeatGenerationService();
                        seatGenService.regenerateSeatsForSection(section.getSectionId());
                        
                        Platform.runLater(() -> {
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Success");
                            successAlert.setHeaderText("Seats Synchronized");
                            successAlert.setContentText(
                                "Successfully regenerated " + section.getTotalCapacity() + 
                                " seats for " + section.getSectionName() + "!\n\n" +
                                "Configuration:\n" +
                                "• Rows: " + section.getTotalRows() + "\n" +
                                "• Seats per row: " + section.getSeatsPerRow() + "\n" +
                                "• Total seats: " + section.getTotalCapacity()
                            );
                            successAlert.showAndWait();
                            
                            // Reload data to show updated info
                            loadStadiumData();
                        });
                        
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Sync Failed");
                            errorAlert.setHeaderText("Failed to Synchronize Seats");
                            
                            // Check if error is due to existing bookings
                            if (e.getMessage() != null && e.getMessage().contains("existing bookings")) {
                                errorAlert.setContentText(
                                    "Cannot regenerate seats because some seats have existing bookings.\n\n" +
                                    "Please cancel or complete those bookings first, then try again."
                                );
                            } else {
                                errorAlert.setContentText("An error occurred: " + e.getMessage());
                            }
                            
                            errorAlert.showAndWait();
                        });
                    }
                }).start();
            }
        });
    }
}
