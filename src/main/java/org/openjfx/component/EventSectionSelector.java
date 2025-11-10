package org.openjfx.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.openjfx.model.EventSection;
import org.openjfx.service.EventSectionService;
import org.openjfx.util.IconUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import java.util.List;
import java.util.function.Consumer;

/**
 * Component for selecting an event section
 * Displays available sections as clickable cards with pricing and capacity
 */
public class EventSectionSelector extends VBox {
    
    private final int eventId;
    private VBox sectionsContainer;
    private Consumer<EventSection> onSectionSelected;
    
    private final EventSectionService eventSectionService;
    
    public EventSectionSelector(int eventId) {
        this.eventId = eventId;
        this.eventSectionService = new EventSectionService();
        
        initializeUI();
        loadSections();
    }
    
    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white;");
        
        // Header
        Label headerLabel = new Label("Select Section");
        headerLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Label subLabel = new Label("Choose your preferred seating section");
        subLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        // Sections container
        sectionsContainer = new VBox(15);
        sectionsContainer.setPadding(new Insets(10, 0, 0, 0));
        
        ScrollPane scrollPane = new ScrollPane(sectionsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        getChildren().addAll(headerLabel, subLabel, scrollPane);
    }
    
    private void loadSections() {
        new Thread(() -> {
            try {
                System.out.println("Loading sections for event ID: " + eventId);
                List<EventSection> sections = eventSectionService.getEventSections(eventId);
                System.out.println("Found " + sections.size() + " sections");
                
                Platform.runLater(() -> {
                    if (sections.isEmpty()) {
                        System.out.println("No sections found in database");
                        showEmptyState();
                    } else {
                        populateSections(sections);
                    }
                });
                
            } catch (Exception e) {
                System.err.println("Error loading sections: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(this::showErrorState);
            }
        }).start();
    }
    
    private void populateSections(List<EventSection> sections) {
        sectionsContainer.getChildren().clear();
        
        int availableCount = 0;
        for (EventSection section : sections) {
            System.out.println("Section: " + section.getSectionTitle() + 
                             ", Available: " + section.getAvailableSeats() + 
                             ", Total: " + section.getTotalCapacity());
            
            if (section.hasAvailableSeats()) {
                availableCount++;
                SectionCard card = new SectionCard(section);
                card.setOnMouseClicked(e -> {
                    if (onSectionSelected != null) {
                        onSectionSelected.accept(section);
                    }
                });
                sectionsContainer.getChildren().add(card);
            }
        }
        
        // If no sections with available seats, show message
        if (availableCount == 0) {
            System.out.println("All sections are fully booked");
            showFullyBookedState();
        }
    }
    
    private void showEmptyState() {
        Label emptyLabel = new Label("No sections available for this event");
        emptyLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #95a5a6;" +
            "-fx-padding: 20px;"
        );
        sectionsContainer.getChildren().add(emptyLabel);
    }
    
    private void showFullyBookedState() {
        Label fullLabel = new Label("All sections are fully booked for this event");
        fullLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #FF9800;" +
            "-fx-padding: 20px;"
        );
        sectionsContainer.getChildren().add(fullLabel);
    }
    
    private void showErrorState() {
        Label errorLabel = new Label("Failed to load sections");
        errorLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #e74c3c;" +
            "-fx-padding: 20px;"
        );
        sectionsContainer.getChildren().add(errorLabel);
    }
    
    public void setOnSectionSelected(Consumer<EventSection> handler) {
        this.onSectionSelected = handler;
    }
    
    /**
     * Individual section card component
     */
    private static class SectionCard extends VBox {
        
        private final EventSection section;
        
        public SectionCard(EventSection section) {
            this.section = section;
            initializeUI();
        }
        
        private void initializeUI() {
            setSpacing(10);
            setPadding(new Insets(20));
            setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 10px;" +
                "-fx-background-radius: 10px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
            
            // Hover effect
            setOnMouseEntered(e -> {
                setStyle(
                    "-fx-background-color: #f8f9fa;" +
                    "-fx-border-color: #2196F3;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.3), 10, 0, 0, 3);"
                );
            });
            
            setOnMouseExited(e -> {
                setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #e0e0e0;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
                );
            });
            
            // Header with title and type
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label icon = new Label();
            icon.setGraphic(IconUtil.createIcon(FontAwesomeIcon.MAP_MARKER, 20));
            icon.setStyle("-fx-text-fill: #2196F3;");
            
            Label titleLabel = new Label(section.getSectionTitle());
            titleLabel.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2c3e50;"
            );
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label typeLabel = new Label(section.getSectionType());
            typeLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: " + (section.getSectionType().equals("TRIBUNE") ? "#2196F3" : "#4CAF50") + ";" +
                "-fx-padding: 4px 10px;" +
                "-fx-background-radius: 12px;"
            );
            
            header.getChildren().addAll(icon, titleLabel, spacer, typeLabel);
            
            // Section name subtitle
            Label sectionNameLabel = new Label(section.getSectionName());
            sectionNameLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #7f8c8d;"
            );
            
            // Stats row
            HBox stats = new HBox(30);
            stats.setAlignment(Pos.CENTER_LEFT);
            stats.setPadding(new Insets(10, 0, 0, 0));
            
            // Price
            VBox priceBox = createStatBox(
                "Price",
                "Rp " + String.format("%,.0f", section.getPrice()),
                "#2196F3"
            );
            
            // Capacity
            VBox capacityBox = createStatBox(
                "Available",
                section.getAvailableSeats() + " / " + section.getTotalCapacity(),
                "#4CAF50"
            );
            
            // Occupancy
            VBox occupancyBox = createStatBox(
                "Occupancy",
                String.format("%.0f%%", section.getOccupancyRate()),
                section.getOccupancyRate() > 80 ? "#FF9800" : "#2196F3"
            );
            
            stats.getChildren().addAll(priceBox, capacityBox, occupancyBox);
            
            // Progress bar
            ProgressBar progressBar = new ProgressBar(section.getTotalCapacity(), section.getAvailableSeats());
            
            getChildren().addAll(header, sectionNameLabel, stats, progressBar);
        }
        
        private VBox createStatBox(String label, String value, String color) {
            VBox box = new VBox(5);
            box.setAlignment(Pos.CENTER_LEFT);
            
            Label labelNode = new Label(label);
            labelNode.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #95a5a6;"
            );
            
            Label valueNode = new Label(value);
            valueNode.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + color + ";"
            );
            
            box.getChildren().addAll(labelNode, valueNode);
            return box;
        }
    }
    
    /**
     * Simple progress bar component
     */
    private static class ProgressBar extends HBox {
        
        public ProgressBar(int total, int available) {
            setAlignment(Pos.CENTER_LEFT);
            setStyle(
                "-fx-background-color: #e0e0e0;" +
                "-fx-background-radius: 5px;" +
                "-fx-pref-height: 8px;"
            );
            
            double percentage = total > 0 ? ((total - available) / (double) total) * 100 : 0;
            
            Region filled = new Region();
            filled.setStyle(
                "-fx-background-color: " + getColorForPercentage(percentage) + ";" +
                "-fx-background-radius: 5px;"
            );
            filled.prefWidthProperty().bind(widthProperty().multiply(percentage / 100.0));
            
            getChildren().add(filled);
        }
        
        private String getColorForPercentage(double percentage) {
            if (percentage >= 90) return "#f44336"; // Red - almost full
            if (percentage >= 70) return "#FF9800"; // Orange - filling up
            return "#4CAF50"; // Green - lots of space
        }
    }
}
