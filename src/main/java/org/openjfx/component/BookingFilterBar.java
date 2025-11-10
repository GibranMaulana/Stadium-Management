package org.openjfx.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.util.function.Consumer;

/**
 * Filter bar for bookings management
 */
public class BookingFilterBar extends VBox {
    
    private final TextField searchField;
    private final ComboBox<String> bookingStatusFilter;
    private Consumer<FilterCriteria> onFilterChanged;
    
    public BookingFilterBar() {
        this.searchField = new TextField();
        this.bookingStatusFilter = new ComboBox<>();
        
        initializeFilters();
        setupLayout();
    }
    
    private void initializeFilters() {
        // Search field
        searchField.setPromptText("Search by booking #, customer name, or email...");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> fireFilterChanged());
        
        // Booking status filter
        bookingStatusFilter.getItems().addAll("All Status", "CONFIRMED", "CANCELLED");
        bookingStatusFilter.setValue("All Status");
        bookingStatusFilter.setPrefWidth(150);
        bookingStatusFilter.setOnAction(e -> fireFilterChanged());
    }
    
    private void setupLayout() {
        setPadding(new Insets(15));
        setSpacing(15);
        setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        
        // Search row
        HBox searchRow = new HBox(15);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView searchIcon = new FontAwesomeIconView(FontAwesomeIcon.SEARCH);
        searchIcon.setSize("16");
        searchIcon.setFill(javafx.scene.paint.Color.web("#7f8c8d"));
        
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchRow.getChildren().addAll(searchIcon, searchField);
        
        // Filters row
        HBox filtersRow = new HBox(15);
        filtersRow.setAlignment(Pos.CENTER_LEFT);
        
        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Button clearButton = new Button("Clear Filters");
        clearButton.setStyle(
            "-fx-background-color: #95a5a6;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 6 15;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        
        clearButton.setOnMouseEntered(e -> 
            clearButton.setStyle(
                "-fx-background-color: #7f8c8d;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 6 15;" +
                "-fx-background-radius: 5px;" +
                "-fx-cursor: hand;"
            )
        );
        
        clearButton.setOnMouseExited(e -> 
            clearButton.setStyle(
                "-fx-background-color: #95a5a6;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 6 15;" +
                "-fx-background-radius: 5px;" +
                "-fx-cursor: hand;"
            )
        );
        
        clearButton.setOnAction(e -> clearFilters());
        
        filtersRow.getChildren().addAll(
            statusLabel, bookingStatusFilter,
            clearButton
        );
        
        getChildren().addAll(searchRow, filtersRow);
    }
    
    /**
     * Clear all filters
     */
    private void clearFilters() {
        searchField.clear();
        bookingStatusFilter.setValue("All Status");
    }
    
    /**
     * Fire filter changed event
     */
    private void fireFilterChanged() {
        if (onFilterChanged != null) {
            FilterCriteria criteria = new FilterCriteria(
                searchField.getText().trim(),
                getSelectedBookingStatus()
            );
            onFilterChanged.accept(criteria);
        }
    }
    
    /**
     * Get selected booking status (null if "All")
     */
    private String getSelectedBookingStatus() {
        String selected = bookingStatusFilter.getValue();
        return "All Status".equals(selected) ? null : selected;
    }
    
    /**
     * Set callback for filter changes
     */
    public void setOnFilterChanged(Consumer<FilterCriteria> callback) {
        this.onFilterChanged = callback;
    }
    
    /**
     * Filter criteria class
     */
    public static class FilterCriteria {
        public final String searchText;
        public final String bookingStatus;
        
        public FilterCriteria(String searchText, String bookingStatus) {
            this.searchText = searchText;
            this.bookingStatus = bookingStatus;
        }
    }
}
