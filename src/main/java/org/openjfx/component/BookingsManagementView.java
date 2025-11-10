package org.openjfx.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.openjfx.model.Booking;
import org.openjfx.service.BookingService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main view for bookings management
 */
public class BookingsManagementView extends VBox {
    
    private final BookingService bookingService;
    private final BookingFilterBar filterBar;
    private final BookingsTableView tableView;
    private final Label totalBookingsLabel;
    private final Label totalRevenueLabel;
    private final ProgressIndicator loadingIndicator;
    
    private List<Booking> allBookings;
    
    public BookingsManagementView() {
        this.bookingService = new BookingService();
        this.filterBar = new BookingFilterBar();
        this.tableView = new BookingsTableView();
        this.totalBookingsLabel = new Label("0");
        this.totalRevenueLabel = new Label("Rp 0");
        this.loadingIndicator = new ProgressIndicator();
        
        initializeUI();
        loadBookings();
    }
    
    private void initializeUI() {
        setSpacing(0);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #ecf0f1;");
        
        // Header
        VBox header = createHeader();
        
        // Summary cards
        HBox summaryCards = createSummaryCards();
        
        // Filter bar
        filterBar.setOnFilterChanged(criteria -> applyFilters(criteria));
        
        // Action buttons
        HBox actionButtons = createActionButtons();
        
        // Table
        tableView.setOnBookingSelected(this::handleViewDetails);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        // Loading indicator
        loadingIndicator.setVisible(false);
        StackPane loadingPane = new StackPane(loadingIndicator);
        loadingPane.setStyle("-fx-background-color: rgba(236, 240, 241, 0.8);");
        loadingPane.setVisible(false);
        loadingPane.setId("loadingPane");
        
        // Container with relative positioning for loading overlay
        StackPane contentPane = new StackPane();
        VBox mainContent = new VBox(0);
        mainContent.getChildren().addAll(header, summaryCards, filterBar, actionButtons, tableView);
        contentPane.getChildren().addAll(mainContent, loadingPane);
        
        getChildren().add(contentPane);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(30, 30, 20, 30));
        header.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.LIST_ALT);
        icon.setSize("28");
        icon.setFill(javafx.scene.paint.Color.web("#2c3e50"));
        
        Label titleLabel = new Label("Bookings Management");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        titleBox.getChildren().addAll(icon, titleLabel);
        
        Label subtitleLabel = new Label("View, manage, and track all customer bookings");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        header.getChildren().addAll(titleBox, subtitleLabel);
        return header;
    }
    
    private HBox createSummaryCards() {
        HBox cards = new HBox(20);
        cards.setPadding(new Insets(20, 30, 0, 30));
        cards.setAlignment(Pos.CENTER);
        
        // Total Bookings Card
        VBox totalCard = createSummaryCard(
            "Total Bookings",
            "0",
            "#3498db",
            FontAwesomeIcon.TICKET,
            totalBookingsLabel
        );
        
        // Total Revenue Card
        VBox revenueCard = createSummaryCard(
            "Total Revenue",
            "Rp 0",
            "#27ae60",
            FontAwesomeIcon.MONEY,
            totalRevenueLabel
        );
        
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        
        cards.getChildren().addAll(totalCard, revenueCard);
        return cards;
    }
    
    private VBox createSummaryCard(String title, String value, String color, 
                                   FontAwesomeIcon icon, Label valueLabel) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("32");
        iconView.setFill(javafx.scene.paint.Color.web(color));
        
        valueLabel.setText(value);
        valueLabel.setId("capacityValue");
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        card.getChildren().addAll(iconView, valueLabel, titleLabel);
        return card;
    }
    
    private HBox createActionButtons() {
        HBox buttons = new HBox(15);
        buttons.setPadding(new Insets(15, 30, 15, 30));
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        
        Button viewDetailsButton = createActionButton(
            "View Details",
            "#3498db",
            "#2980b9",
            FontAwesomeIcon.EYE
        );
        viewDetailsButton.setOnAction(e -> handleViewDetails(tableView.getSelectedBooking()));
        
        Button refreshButton = createActionButton(
            "Refresh",
            "#95a5a6",
            "#7f8c8d",
            FontAwesomeIcon.REFRESH
        );
        refreshButton.setOnAction(e -> loadBookings());
        
        // Add spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label infoLabel = new Label("💡 Double-click a row to view details");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-font-style: italic;");
        
        buttons.getChildren().addAll(viewDetailsButton, refreshButton, spacer, infoLabel);
        return buttons;
    }
    
    private Button createActionButton(String text, String bgColor, String hoverColor, FontAwesomeIcon icon) {
        Button button = new Button(text);
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("14");
        iconView.setFill(javafx.scene.paint.Color.WHITE);
        button.setGraphic(iconView);
        
        button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: " + hoverColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        ));
        
        return button;
    }
    
    private void loadBookings() {
        showLoading(true);
        
        new Thread(() -> {
            try {
                List<Booking> bookings = bookingService.getAllBookings();
                
                Platform.runLater(() -> {
                    allBookings = bookings;
                    tableView.setBookings(bookings);
                    updateSummary(bookings);
                    showLoading(false);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Failed to load bookings: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void applyFilters(BookingFilterBar.FilterCriteria criteria) {
        if (allBookings == null) return;
        
        List<Booking> filtered = allBookings.stream()
            .filter(booking -> matchesSearchCriteria(booking, criteria.searchText))
            .filter(booking -> matchesBookingStatus(booking, criteria.bookingStatus))
            .collect(Collectors.toList());
        
        tableView.setBookings(filtered);
        updateSummary(filtered);
    }
    
    private boolean matchesSearchCriteria(Booking booking, String searchText) {
        if (searchText == null || searchText.isEmpty()) return true;
        
        String search = searchText.toLowerCase();
        return booking.getBookingNumber().toLowerCase().contains(search) ||
               booking.getCustomerName().toLowerCase().contains(search) ||
               booking.getCustomerEmail().toLowerCase().contains(search);
    }
    
    private boolean matchesBookingStatus(Booking booking, String bookingStatus) {
        return bookingStatus == null || booking.getBookingStatus().equals(bookingStatus);
    }
    
    private void updateSummary(List<Booking> bookings) {
        int totalBookings = bookings.size();
        double totalRevenue = bookings.stream()
            .filter(b -> "CONFIRMED".equals(b.getBookingStatus()))
            .mapToDouble(Booking::getTotalPrice)
            .sum();
        
        totalBookingsLabel.setText(String.valueOf(totalBookings));
        totalRevenueLabel.setText(String.format("Rp %,.0f", totalRevenue));
    }
    
    private void handleViewDetails(Booking booking) {
        if (booking == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Booking Selected");
            alert.setContentText("Please select a booking to view details.");
            alert.showAndWait();
            return;
        }
        
        BookingDetailsDialog dialog = new BookingDetailsDialog(booking);
        dialog.setOnBookingUpdated(updatedBooking -> {
            // Refresh table to show updated status
            tableView.refresh();
            if (allBookings != null) {
                updateSummary(allBookings);
            }
        });
        dialog.showAndWait();
    }
    
    private void showLoading(boolean show) {
        StackPane loadingPane = (StackPane) lookup("#loadingPane");
        if (loadingPane != null) {
            loadingPane.setVisible(show);
            loadingPane.toFront();
        }
        loadingIndicator.setVisible(show);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to Load Bookings");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
