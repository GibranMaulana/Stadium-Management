package org.openjfx.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import org.openjfx.model.Booking;
import org.openjfx.model.BookingSeat;
import org.openjfx.service.BookingService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Dialog showing complete booking details
 */
public class BookingDetailsDialog extends Dialog<ButtonType> {
    
    private final Booking booking;
    private final BookingService bookingService;
    private Consumer<Booking> onBookingUpdated;
    
    public BookingDetailsDialog(Booking booking) {
        this.booking = booking;
        this.bookingService = new BookingService();
        
        initializeDialog();
        loadBookingDetails();
    }
    
    private void initializeDialog() {
        setTitle("Booking Details");
        setHeaderText("Booking #" + booking.getBookingNumber());
        initModality(Modality.APPLICATION_MODAL);
        
        // Create content
        VBox content = createContent();
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(700, 600);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        
        getDialogPane().setContent(scrollPane);
        
        // Add buttons
        ButtonType deleteBookingBtn = new ButtonType("Delete Booking", ButtonBar.ButtonData.LEFT);
        ButtonType cancelBookingBtn = new ButtonType("Cancel Booking", ButtonBar.ButtonData.LEFT);
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        
        getDialogPane().getButtonTypes().addAll(deleteBookingBtn, cancelBookingBtn, closeBtn);
        
        // Style and handle buttons
        Button deleteBtn = (Button) getDialogPane().lookupButton(deleteBookingBtn);
        Button cancelBtn = (Button) getDialogPane().lookupButton(cancelBookingBtn);
        
        deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Disable cancel button if already cancelled
        if ("CANCELLED".equals(booking.getBookingStatus())) {
            cancelBtn.setDisable(true);
        }
        
        deleteBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            handleDeleteBooking();
        });
        
        cancelBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            handleCancelBooking();
        });
    }
    
    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        // Customer info section
        VBox customerSection = createSection("Customer Information", FontAwesomeIcon.USER);
        customerSection.getChildren().addAll(
            createInfoRow("Name:", booking.getCustomerName()),
            createInfoRow("Email:", booking.getCustomerEmail()),
            createInfoRow("Phone:", booking.getCustomerPhone())
        );
        
        // Event info section
        VBox eventSection = createSection("Event Information", FontAwesomeIcon.CALENDAR);
        eventSection.getChildren().addAll(
            createInfoRow("Event:", booking.getEventName() != null ? booking.getEventName() : "Loading...")
        );
        
        // Booking info section
        VBox bookingSection = createSection("Booking Information", FontAwesomeIcon.TICKET);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy 'at' HH:mm");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        bookingSection.getChildren().addAll(
            createInfoRow("Booking Number:", booking.getBookingNumber()),
            createInfoRow("Booking Date:", dateFormat.format(booking.getBookingDate())),
            createInfoRow("Total Seats:", String.valueOf(booking.getTotalSeats())),
            createInfoRow("Total Price:", currencyFormat.format(booking.getTotalPrice())),
            createStatusRow("Booking Status:", booking.getBookingStatus())
        );
        
        // Seats section (will be populated after loading)
        VBox seatsSection = createSection("Seat Details", FontAwesomeIcon.TH);
        seatsSection.setId("seatsSection");
        Label loadingLabel = new Label("Loading seats...");
        loadingLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        seatsSection.getChildren().add(loadingLabel);
        
        content.getChildren().addAll(customerSection, eventSection, bookingSection, seatsSection);
        return content;
    }
    
    private VBox createSection(String title, FontAwesomeIcon icon) {
        VBox section = new VBox(10);
        section.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-padding: 15;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("18");
        iconView.setFill(javafx.scene.paint.Color.web("#3498db"));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        header.getChildren().addAll(iconView, titleLabel);
        section.getChildren().add(header);
        
        return section;
    }
    
    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-min-width: 150px;");
        
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #2c3e50;");
        valueText.setWrapText(true);
        
        row.getChildren().addAll(labelText, valueText);
        return row;
    }
    
    private HBox createStatusRow(String label, String status) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-min-width: 150px;");
        
        Label statusLabel = new Label(status);
        statusLabel.setPadding(new Insets(5, 15, 5, 15));
        statusLabel.setStyle("-fx-font-weight: bold; -fx-background-radius: 15px;");
        
        // Color based on status
        if ("PAID".equals(status) || "CONFIRMED".equals(status)) {
            statusLabel.setStyle(statusLabel.getStyle() + 
                "-fx-background-color: #d4edda; -fx-text-fill: #155724;");
        } else if ("CANCELLED".equals(status)) {
            statusLabel.setStyle(statusLabel.getStyle() + 
                "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
        } else {
            statusLabel.setStyle(statusLabel.getStyle() + 
                "-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
        }
        
        row.getChildren().addAll(labelText, statusLabel);
        return row;
    }
    
    private void loadBookingDetails() {
        new Thread(() -> {
            try {
                List<BookingSeat> seats = bookingService.getBookingSeats(booking.getBookingId());
                
                Platform.runLater(() -> {
                    // Find seats section
                    VBox seatsSection = (VBox) getDialogPane().getContent().lookup("#seatsSection");
                    if (seatsSection != null) {
                        // Remove loading label
                        seatsSection.getChildren().remove(1);
                        
                        // Add seats grid
                        if (seats != null && !seats.isEmpty()) {
                            GridPane seatsGrid = new GridPane();
                            seatsGrid.setHgap(10);
                            seatsGrid.setVgap(8);
                            seatsGrid.setPadding(new Insets(5, 0, 0, 0));
                            
                            // Header row
                            Label headerSection = new Label("Section");
                            headerSection.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                            Label headerSeat = new Label("Seat");
                            headerSeat.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                            Label headerPrice = new Label("Price");
                            headerPrice.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                            
                            seatsGrid.add(headerSection, 0, 0);
                            seatsGrid.add(headerSeat, 1, 0);
                            seatsGrid.add(headerPrice, 2, 0);
                            
                            // Seat rows
                            int row = 1;
                            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                            
                            for (BookingSeat seat : seats) {
                                Label sectionLabel = new Label(seat.getSectionName());
                                sectionLabel.setStyle("-fx-text-fill: #2c3e50;");
                                
                                String seatInfo = seat.getRowNumber() != null && seat.getSeatNumber() != 0
                                    ? seat.getRowNumber() + "-" + seat.getSeatNumber()
                                    : "Standing";
                                Label seatLabel = new Label(seatInfo);
                                seatLabel.setStyle("-fx-text-fill: #2c3e50;");
                                
                                Label priceLabel = new Label(currencyFormat.format(seat.getPrice()));
                                priceLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                
                                seatsGrid.add(sectionLabel, 0, row);
                                seatsGrid.add(seatLabel, 1, row);
                                seatsGrid.add(priceLabel, 2, row);
                                row++;
                            }
                            
                            seatsSection.getChildren().add(seatsGrid);
                        } else {
                            Label noSeatsLabel = new Label("No seat details available");
                            noSeatsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                            seatsSection.getChildren().add(noSeatsLabel);
                        }
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    VBox seatsSection = (VBox) getDialogPane().getContent().lookup("#seatsSection");
                    if (seatsSection != null && seatsSection.getChildren().size() > 1) {
                        seatsSection.getChildren().remove(1);
                        Label errorLabel = new Label("Failed to load seat details");
                        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
                        seatsSection.getChildren().add(errorLabel);
                    }
                });
            }
        }).start();
    }
    
    private void handleDeleteBooking() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Delete Booking");
        confirm.setHeaderText("Are you sure you want to PERMANENTLY DELETE this booking?");
        confirm.setContentText("Booking #" + booking.getBookingNumber() + 
                              "\n\nThis will permanently remove the booking from the database." +
                              "\nThis action CANNOT be undone!" +
                              "\n\nIf you want to keep the record, use 'Cancel Booking' instead.");
        
        // Make it clear this is dangerous
        confirm.getDialogPane().setStyle("-fx-background-color: #fee;");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    boolean success = bookingService.deleteBooking(booking.getBookingId());
                    Platform.runLater(() -> {
                        if (success) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText("Booking Deleted");
                            alert.setContentText("The booking has been permanently deleted from the database.");
                            alert.showAndWait();
                            
                            if (onBookingUpdated != null) {
                                onBookingUpdated.accept(booking);
                            }
                            
                            close();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Deletion Failed");
                            alert.setContentText("Failed to delete booking. Please try again.");
                            alert.showAndWait();
                        }
                    });
                }).start();
            }
        });
    }
    
    private void handleCancelBooking() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Booking");
        confirm.setHeaderText("Are you sure you want to cancel this booking?");
        confirm.setContentText("Booking #" + booking.getBookingNumber() + "\nThis action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    boolean success = bookingService.cancelBooking(booking.getBookingId());
                    Platform.runLater(() -> {
                        if (success) {
                            booking.setBookingStatus("CANCELLED");
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText("Booking Cancelled");
                            alert.setContentText("The booking has been cancelled successfully.");
                            alert.showAndWait();
                            
                            if (onBookingUpdated != null) {
                                onBookingUpdated.accept(booking);
                            }
                            
                            close();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Cancellation Failed");
                            alert.setContentText("Failed to cancel booking. Please try again.");
                            alert.showAndWait();
                        }
                    });
                }).start();
            }
        });
    }
    
    /**
     * Set callback for when booking is updated
     */
    public void setOnBookingUpdated(Consumer<Booking> callback) {
        this.onBookingUpdated = callback;
    }
}
