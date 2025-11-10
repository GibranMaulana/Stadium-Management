package org.openjfx.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.openjfx.model.Seat;
import org.openjfx.model.Section;
import org.openjfx.service.SeatService;
import org.openjfx.service.SectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Component displaying a grid of seats for a tribune section
 * Dynamically shows seats based on section configuration
 */
public class SeatMapGrid extends VBox {
    
    private final int eventId;
    private final int sectionId;
    private final String sectionName;
    private final double pricePerSeat;
    
    private GridPane seatGrid;
    private Label selectedCountLabel;
    private Label totalPriceLabel;
    private List<SeatButton> seatButtons;
    
    private final SeatService seatService;
    private final SectionService sectionService;
    
    private int totalRows;
    private int seatsPerRow;
    
    // Callback for selection changes
    private Runnable onSelectionChanged;
    
    public SeatMapGrid(int eventId, int sectionId, String sectionName, double pricePerSeat) {
        this.eventId = eventId;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.pricePerSeat = pricePerSeat;
        this.seatButtons = new ArrayList<>();
        this.seatService = new SeatService();
        this.sectionService = new SectionService();
        
        // Load section dimensions
        loadSectionDimensions();
        
        initializeUI();
        loadSeats();
    }
    
    private void loadSectionDimensions() {
        try {
            Section section = sectionService.getSectionById(sectionId);
            if (section != null) {
                this.totalRows = section.getTotalRows();
                this.seatsPerRow = section.getSeatsPerRow();
                System.out.println("DEBUG: SeatMapGrid loaded section dimensions - " +
                                 "Rows=" + totalRows + ", SeatsPerRow=" + seatsPerRow);
            } else {
                // Fallback to defaults if section not found
                this.totalRows = 30;
                this.seatsPerRow = 25;
                System.err.println("WARNING: Section not found, using default dimensions");
            }
        } catch (Exception e) {
            // Fallback to defaults on error
            this.totalRows = 30;
            this.seatsPerRow = 25;
            System.err.println("ERROR: Failed to load section dimensions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white;");
        
        // Header
        Label headerLabel = new Label(sectionName + " - Seat Selection");
        headerLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        // Legend
        HBox legend = createLegend();
        
        // Seat grid container with scroll
        seatGrid = new GridPane();
        seatGrid.setHgap(5);
        seatGrid.setVgap(5);
        seatGrid.setAlignment(Pos.CENTER);
        seatGrid.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(seatGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(500);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");
        
        // Selection summary
        VBox summary = createSummary();
        
        getChildren().addAll(headerLabel, legend, scrollPane, summary);
    }
    
    private HBox createLegend() {
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(10));
        legend.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5px;");
        
        // Available
        HBox available = createLegendItem("#4CAF50", "Available");
        
        // Selected
        HBox selected = createLegendItem("#2196F3", "Selected");
        
        // Booked
        HBox booked = createLegendItem("#f44336", "Booked");
        
        legend.getChildren().addAll(available, selected, booked);
        return legend;
    }
    
    private HBox createLegendItem(String color, String label) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Region colorBox = new Region();
        colorBox.setMinSize(20, 20);
        colorBox.setMaxSize(20, 20);
        colorBox.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 3px;"
        );
        
        Label text = new Label(label);
        text.setStyle("-fx-font-size: 12px;");
        
        item.getChildren().addAll(colorBox, text);
        return item;
    }
    
    private VBox createSummary() {
        VBox summary = new VBox(10);
        summary.setAlignment(Pos.CENTER);
        summary.setPadding(new Insets(15));
        summary.setStyle(
            "-fx-background-color: #e3f2fd;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #2196F3;" +
            "-fx-border-radius: 5px;" +
            "-fx-border-width: 2px;"
        );
        
        selectedCountLabel = new Label("Selected: 0 seats");
        selectedCountLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1976D2;"
        );
        
        totalPriceLabel = new Label("Total: Rp 0");
        totalPriceLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1565C0;"
        );
        
        summary.getChildren().addAll(selectedCountLabel, totalPriceLabel);
        return summary;
    }
    
    private void loadSeats() {
        new Thread(() -> {
            try {
                // Get all available seats for this section in this event
                List<Seat> seats = seatService.getAvailableSeats(eventId, sectionId);
                
                Platform.runLater(() -> {
                    populateSeatGrid(seats);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load seats");
                    errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                    seatGrid.add(errorLabel, 0, 0);
                });
            }
        }).start();
    }
    
    private void populateSeatGrid(List<Seat> availableSeats) {
        seatGrid.getChildren().clear();
        seatButtons.clear();
        
        // Get all seats for this section (both available and booked)
        List<Seat> allSeats;
        try {
            allSeats = seatService.getSeatsBySection(sectionId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // Use actual section dimensions
        String[] rows = generateRowLabels(totalRows);
        
        System.out.println("DEBUG: Populating seat grid - " +
                         "Rows=" + totalRows + ", SeatsPerRow=" + seatsPerRow +
                         ", TotalSeats=" + allSeats.size());
        
        // Check actual seat distribution
        if (!allSeats.isEmpty()) {
            int maxSeatNum = allSeats.stream()
                .mapToInt(Seat::getSeatNumber)
                .max()
                .orElse(0);
            System.out.println("DEBUG: Maximum seat number in database: " + maxSeatNum);
            
            // Sample first row
            String firstRow = rows[0];
            long seatsInFirstRow = allSeats.stream()
                .filter(s -> s.getRowNumber().equals(firstRow))
                .count();
            System.out.println("DEBUG: Seats in first row (" + firstRow + "): " + seatsInFirstRow);
        }
        
        // Add column headers (seat numbers)
        for (int col = 0; col < seatsPerRow; col++) {
            Label colLabel = new Label(String.valueOf(col + 1));
            colLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #666;"
            );
            colLabel.setMinWidth(35);
            colLabel.setAlignment(Pos.CENTER);
            seatGrid.add(colLabel, col + 1, 0);
        }
        
        // Add seats with row labels
        for (int row = 0; row < rows.length; row++) {
            String rowLabel = rows[row];
            
            // Row label
            Label rowLabelNode = new Label(rowLabel);
            rowLabelNode.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #666;"
            );
            rowLabelNode.setMinWidth(30);
            rowLabelNode.setAlignment(Pos.CENTER);
            seatGrid.add(rowLabelNode, 0, row + 1);
            
            // Seats in this row
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                final String currentRow = rowLabel;
                final int currentSeatNum = seatNum;
                
                // Find the seat from database
                Seat seat = allSeats.stream()
                    .filter(s -> s.getRowNumber().equals(currentRow) && 
                                 s.getSeatNumber() == currentSeatNum)
                    .findFirst()
                    .orElse(null);
                
                if (seat != null) {
                    // Check if available in this event
                    boolean isAvailable = availableSeats.stream()
                        .anyMatch(s -> s.getSeatId() == seat.getSeatId());
                    
                    if (!isAvailable) {
                        seat.setStatus("BOOKED");
                    }
                    
                    SeatButton seatButton = new SeatButton(seat);
                    // Add additional action after the button's internal handler
                    seatButton.setOnAction(e -> {
                        // First, toggle the seat state (handled by SeatButton internally)
                        if (seatButton.getState() == SeatButton.SeatState.AVAILABLE) {
                            seatButton.setState(SeatButton.SeatState.SELECTED);
                        } else if (seatButton.getState() == SeatButton.SeatState.SELECTED) {
                            seatButton.setState(SeatButton.SeatState.AVAILABLE);
                        }
                        // Then update the summary
                        updateSummary();
                    });
                    seatButtons.add(seatButton);
                    
                    seatGrid.add(seatButton, seatNum, row + 1);
                }
            }
        }
    }
    
    private String[] generateRowLabels(int totalRows) {
        String[] labels = new String[totalRows];
        
        // First 26 rows: A-Z
        for (int i = 0; i < Math.min(26, totalRows); i++) {
            labels[i] = String.valueOf((char) ('A' + i));
        }
        
        // Remaining rows: AA, AB, AC, AD
        for (int i = 26; i < totalRows; i++) {
            int index = i - 26;
            labels[i] = "A" + (char) ('A' + index);
        }
        
        return labels;
    }
    
    private void updateSummary() {
        List<SeatButton> selectedSeats = getSelectedSeats();
        int count = selectedSeats.size();
        double total = count * pricePerSeat;
        
        selectedCountLabel.setText("Selected: " + count + " seat" + (count != 1 ? "s" : ""));
        totalPriceLabel.setText("Total: Rp " + String.format("%,.0f", total));
        
        // Notify listeners of selection change
        if (onSelectionChanged != null) {
            onSelectionChanged.run();
        }
    }
    
    /**
     * Set callback for when seat selection changes
     */
    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback;
    }
    
    public List<SeatButton> getSelectedSeats() {
        return seatButtons.stream()
            .filter(SeatButton::isSelected)
            .collect(Collectors.toList());
    }
    
    public List<Seat> getSelectedSeatModels() {
        return getSelectedSeats().stream()
            .map(SeatButton::getSeat)
            .collect(Collectors.toList());
    }
    
    public double getTotalPrice() {
        return getSelectedSeats().size() * pricePerSeat;
    }
    
    public void clearSelection() {
        seatButtons.forEach(btn -> {
            if (btn.isSelected()) {
                btn.setState(SeatButton.SeatState.AVAILABLE);
            }
        });
        updateSummary();
    }
}
