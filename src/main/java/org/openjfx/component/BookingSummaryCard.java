package org.openjfx.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import org.openjfx.model.Seat;
import org.openjfx.util.IconUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import java.util.List;

/**
 * Component displaying booking summary with selected seats and pricing
 */
public class BookingSummaryCard extends VBox {
    
    private final String eventName;
    private final String sectionTitle;
    private final List<Seat> selectedSeats;
    private final double pricePerSeat;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    
    public BookingSummaryCard(String eventName, String sectionTitle, 
                             List<Seat> selectedSeats, double pricePerSeat,
                             String customerName, String customerEmail, String customerPhone) {
        this.eventName = eventName;
        this.sectionTitle = sectionTitle;
        this.selectedSeats = selectedSeats;
        this.pricePerSeat = pricePerSeat;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(25));
        setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #2196F3;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.2), 10, 0, 0, 3);"
        );
        
        // Header
        Label headerLabel = new Label("Booking Summary");
        headerLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Label subLabel = new Label("Please review your booking details before confirming");
        subLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        // Event info section
        VBox eventSection = createSection("Event Details", FontAwesomeIcon.CALENDAR);
        eventSection.getChildren().addAll(
            createInfoRow("Event", eventName),
            createInfoRow("Section", sectionTitle)
        );
        
        // Customer info section
        VBox customerSection = createSection("Customer Information", FontAwesomeIcon.USER);
        customerSection.getChildren().addAll(
            createInfoRow("Name", customerName),
            createInfoRow("Email", customerEmail),
            createInfoRow("Phone", customerPhone)
        );
        
        // Seats section
        VBox seatsSection = createSection("Selected Seats", FontAwesomeIcon.TICKET);
        
        // Group seats display
        VBox seatsDisplay = new VBox(10);
        seatsDisplay.setPadding(new Insets(10, 0, 0, 0));
        
        if (isStandingTicket()) {
            Label standingLabel = new Label(selectedSeats.size() + " Standing Ticket" + 
                                          (selectedSeats.size() > 1 ? "s" : ""));
            standingLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2196F3;"
            );
            seatsDisplay.getChildren().add(standingLabel);
        } else {
            // Show seat grid for tribune tickets
            FlowPane seatsFlow = new FlowPane();
            seatsFlow.setHgap(8);
            seatsFlow.setVgap(8);
            seatsFlow.setPadding(new Insets(5));
            
            for (Seat seat : selectedSeats) {
                Label seatLabel = new Label(seat.getSeatLabel());
                seatLabel.setStyle(
                    "-fx-background-color: #2196F3;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 6px 12px;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;"
                );
                seatsFlow.getChildren().add(seatLabel);
            }
            seatsDisplay.getChildren().add(seatsFlow);
        }
        
        seatsSection.getChildren().add(seatsDisplay);
        
        // Pricing section
        VBox pricingSection = new VBox(10);
        pricingSection.setPadding(new Insets(15));
        pricingSection.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-background-radius: 8px;"
        );
        
        int seatCount = selectedSeats.size();
        double subtotal = seatCount * pricePerSeat;
        double total = subtotal;
        
        pricingSection.getChildren().addAll(
            createPriceRow("Price per seat", pricePerSeat, false),
            createPriceRow("Number of seats", seatCount, false),
            new Separator(),
            createPriceRow("Subtotal", subtotal, false),
            createPriceRow("Total", total, true)
        );
        
        // Confirm info
        HBox confirmBox = new HBox(10);
        confirmBox.setAlignment(Pos.CENTER_LEFT);
        confirmBox.setPadding(new Insets(15));
        confirmBox.setStyle(
            "-fx-background-color: #fff3cd;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #ffc107;" +
            "-fx-border-radius: 5px;" +
            "-fx-border-width: 1px;"
        );
        
        Label warningIcon = new Label();
        warningIcon.setGraphic(IconUtil.createIcon(FontAwesomeIcon.EXCLAMATION_TRIANGLE, 16));
        warningIcon.setStyle("-fx-text-fill: #856404;");
        
        Label confirmText = new Label(
            "Please ensure all information is correct. Booking cannot be modified after confirmation."
        );
        confirmText.setWrapText(true);
        confirmText.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #856404;"
        );
        
        confirmBox.getChildren().addAll(warningIcon, confirmText);
        
        getChildren().addAll(
            headerLabel, 
            subLabel, 
            eventSection, 
            customerSection, 
            seatsSection, 
            pricingSection,
            confirmBox
        );
    }
    
    private VBox createSection(String title, FontAwesomeIcon icon) {
        VBox section = new VBox(12);
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label();
        iconLabel.setGraphic(IconUtil.createIcon(icon, 16));
        iconLabel.setStyle("-fx-text-fill: #2196F3;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        header.getChildren().addAll(iconLabel, titleLabel);
        section.getChildren().add(header);
        
        return section;
    }
    
    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelNode = new Label(label + ":");
        labelNode.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #7f8c8d;" +
            "-fx-min-width: 80px;"
        );
        
        Label valueNode = new Label(value);
        valueNode.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        valueNode.setWrapText(true);
        
        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }
    
    private HBox createPriceRow(String label, double value, boolean isTotal) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelNode = new Label(label);
        labelNode.setStyle(
            "-fx-font-size: " + (isTotal ? "16px" : "13px") + ";" +
            "-fx-font-weight: " + (isTotal ? "bold" : "normal") + ";" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valueNode = new Label("Rp " + String.format("%,.0f", value));
        valueNode.setStyle(
            "-fx-font-size: " + (isTotal ? "18px" : "13px") + ";" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + (isTotal ? "#2196F3" : "#2c3e50") + ";"
        );
        
        row.getChildren().addAll(labelNode, spacer, valueNode);
        return row;
    }
    
    private HBox createPriceRow(String label, int value, boolean isTotal) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelNode = new Label(label);
        labelNode.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valueNode = new Label(String.valueOf(value));
        valueNode.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        row.getChildren().addAll(labelNode, spacer, valueNode);
        return row;
    }
    
    private boolean isStandingTicket() {
        // Standing tickets don't have seat IDs or row/seat numbers
        return selectedSeats.isEmpty() || 
               selectedSeats.get(0).getSeatId() == 0 ||
               selectedSeats.get(0).getRowNumber() == null;
    }
    
    public double getTotalPrice() {
        return selectedSeats.size() * pricePerSeat;
    }
}
