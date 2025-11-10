package org.openjfx.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.openjfx.model.Booking;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Table view component for displaying bookings
 */
public class BookingsTableView extends VBox {
    
    private final TableView<Booking> table;
    private final ObservableList<Booking> bookings;
    private Consumer<Booking> onBookingSelected;
    
    public BookingsTableView() {
        this.bookings = FXCollections.observableArrayList();
        this.table = new TableView<>();
        
        initializeTable();
        setupLayout();
    }
    
    private void initializeTable() {
        table.setItems(bookings);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No bookings found"));
        
        // Booking Number column
        TableColumn<Booking, String> bookingNumCol = new TableColumn<>("Booking #");
        bookingNumCol.setCellValueFactory(new PropertyValueFactory<>("bookingNumber"));
        bookingNumCol.setPrefWidth(120);
        bookingNumCol.setStyle("-fx-alignment: CENTER;");
        
        // Event column
        TableColumn<Booking, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        eventCol.setPrefWidth(200);
        
        // Customer column
        TableColumn<Booking, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setPrefWidth(150);
        
        // Email column
        TableColumn<Booking, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        emailCol.setPrefWidth(180);
        
        // Phone column
        TableColumn<Booking, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        phoneCol.setPrefWidth(120);
        
        // Seats column
        TableColumn<Booking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("totalSeats"));
        seatsCol.setPrefWidth(70);
        seatsCol.setStyle("-fx-alignment: CENTER;");
        
        // Price column
        TableColumn<Booking, Double> priceCol = new TableColumn<>("Total Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        priceCol.setPrefWidth(120);
        priceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        priceCol.setCellFactory(col -> new TableCell<Booking, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    setText(formatter.format(price));
                }
            }
        });
        
        // Booking Status column
        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));
        statusCol.setPrefWidth(120);
        statusCol.setStyle("-fx-alignment: CENTER;");
        statusCol.setCellFactory(col -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("CONFIRMED".equals(status)) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; " +
                               "-fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else { // CANCELLED
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; " +
                               "-fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });
        
        // Booking Date column
        TableColumn<Booking, Timestamp> dateCol = new TableColumn<>("Booking Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER;");
        dateCol.setCellFactory(col -> new TableCell<Booking, Timestamp>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm");
            
            @Override
            protected void updateItem(Timestamp date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(format.format(date));
                }
            }
        });
        
        // Add all columns
        table.getColumns().addAll(
            bookingNumCol, eventCol, customerCol, emailCol, phoneCol,
            seatsCol, priceCol, statusCol, dateCol
        );
        
        // Selection listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && onBookingSelected != null) {
                onBookingSelected.accept(newSelection);
            }
        });
        
        // Row double-click listener
        table.setRowFactory(tv -> {
            TableRow<Booking> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && onBookingSelected != null) {
                    onBookingSelected.accept(row.getItem());
                }
            });
            return row;
        });
    }
    
    private void setupLayout() {
        setPadding(new Insets(0));
        setSpacing(0);
        
        table.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-width: 1px;"
        );
        
        getChildren().add(table);
    }
    
    /**
     * Set the bookings data
     */
    public void setBookings(List<Booking> bookingsList) {
        bookings.clear();
        if (bookingsList != null) {
            bookings.addAll(bookingsList);
        }
    }
    
    /**
     * Get selected booking
     */
    public Booking getSelectedBooking() {
        return table.getSelectionModel().getSelectedItem();
    }
    
    /**
     * Clear selection
     */
    public void clearSelection() {
        table.getSelectionModel().clearSelection();
    }
    
    /**
     * Set callback for booking selection
     */
    public void setOnBookingSelected(Consumer<Booking> callback) {
        this.onBookingSelected = callback;
    }
    
    /**
     * Refresh the table
     */
    public void refresh() {
        table.refresh();
    }
}
