package org.openjfx.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.openjfx.model.Booking;
import org.openjfx.model.Event;
import org.openjfx.model.EventSection;
import org.openjfx.model.Seat;
import org.openjfx.service.BookingService;
import org.openjfx.service.EventService;
import org.openjfx.util.IconUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-step wizard dialog for the complete booking flow
 * Steps: Select Event → Choose Section → Pick Seats → Enter Details → Confirm
 */
public class BookingWizardDialog extends Stage {
    
    // Wizard steps
    private enum Step {
        SELECT_EVENT(1, "Select Event"),
        CHOOSE_SECTION(2, "Choose Section"),
        PICK_SEATS(3, "Pick Seats"),
        ENTER_DETAILS(4, "Customer Details"),
        CONFIRM(5, "Confirm Booking");
        
        final int number;
        final String title;
        
        Step(int number, String title) {
            this.number = number;
            this.title = title;
        }
    }
    
    private Step currentStep = Step.SELECT_EVENT;
    
    // Components
    private VBox contentArea;
    private HBox navigationButtons;
    private Button backButton;
    private Button nextButton;
    private Button cancelButton;
    private HBox stepIndicator;
    
    // Step-specific components
    private VBox eventSelectionView;
    private EventSectionSelector sectionSelector;
    private SeatMapGrid seatMapGrid;
    private CustomerFormPanel customerForm;
    private BookingSummaryCard summaryCard;
    
    // Data
    private Event selectedEvent;
    private EventSection selectedSection;
    private List<Seat> selectedSeats;
    
    // Services
    private final EventService eventService;
    private final BookingService bookingService;
    
    // Callback
    private Runnable onBookingComplete;
    
    public BookingWizardDialog() {
        this.eventService = new EventService();
        this.bookingService = new BookingService();
        this.selectedSeats = new ArrayList<>();
        
        initializeDialog();
        initializeUI();
        loadEvents();
    }
    
    private void initializeDialog() {
        setTitle("Book Tickets - Stadium Booking System");
        initModality(Modality.APPLICATION_MODAL);
        setMinWidth(900);
        setMinHeight(700);
        setResizable(true);
    }
    
    private void initializeUI() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header with step indicator
        VBox header = createHeader();
        
        // Content area
        contentArea = new VBox();
        contentArea.setStyle("-fx-background-color: white;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        // Navigation buttons
        navigationButtons = createNavigationButtons();
        
        root.getChildren().addAll(header, contentArea, navigationButtons);
        
        Scene scene = new Scene(root, 900, 700);
        setScene(scene);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2196F3;");
        
        // Title
        Label titleLabel = new Label("Book Your Tickets");
        titleLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        
        // Step indicator
        stepIndicator = createStepIndicator();
        
        header.getChildren().addAll(titleLabel, stepIndicator);
        return header;
    }
    
    private HBox createStepIndicator() {
        HBox indicator = new HBox(10);
        indicator.setAlignment(Pos.CENTER);
        
        for (Step step : Step.values()) {
            VBox stepBox = createStepBox(step);
            indicator.getChildren().add(stepBox);
            
            // Add arrow between steps (except last)
            if (step != Step.CONFIRM) {
                Label arrow = new Label("→");
                arrow.setStyle(
                    "-fx-font-size: 18px;" +
                    "-fx-text-fill: rgba(255,255,255,0.5);"
                );
                indicator.getChildren().add(arrow);
            }
        }
        
        return indicator;
    }
    
    private VBox createStepBox(Step step) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setMinWidth(120);
        
        boolean isActive = step == currentStep;
        boolean isCompleted = step.number < currentStep.number;
        
        // Step number circle
        Label numberLabel = new Label(String.valueOf(step.number));
        numberLabel.setStyle(
            "-fx-background-color: " + (isActive ? "white" : isCompleted ? "#4CAF50" : "rgba(255,255,255,0.3)") + ";" +
            "-fx-text-fill: " + (isActive || isCompleted ? "#2196F3" : "white") + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8px 12px;" +
            "-fx-background-radius: 20px;" +
            "-fx-min-width: 35px;" +
            "-fx-alignment: center;"
        );
        
        // Step title
        Label titleLabel = new Label(step.title);
        titleLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: " + (isActive ? "white" : "rgba(255,255,255,0.7)") + ";" +
            "-fx-font-weight: " + (isActive ? "bold" : "normal") + ";"
        );
        
        box.getChildren().addAll(numberLabel, titleLabel);
        return box;
    }
    
    private HBox createNavigationButtons() {
        HBox nav = new HBox(10);
        nav.setAlignment(Pos.CENTER_RIGHT);
        nav.setPadding(new Insets(15, 20, 15, 20));
        nav.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        
        cancelButton = new Button("Cancel");
        cancelButton.setStyle(
            "-fx-background-color: #6c757d;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        cancelButton.setOnAction(e -> close());
        
        backButton = new Button("← Back");
        backButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: #2196F3;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #2196F3;" +
            "-fx-border-radius: 5px;" +
            "-fx-border-width: 2px;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> previousStep());
        backButton.setDisable(true);
        
        nextButton = new Button("Next →");
        nextButton.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 30px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        nextButton.setOnAction(e -> nextStep());
        nextButton.setDisable(true);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        nav.getChildren().addAll(cancelButton, spacer, backButton, nextButton);
        return nav;
    }
    
    private void loadEvents() {
        new Thread(() -> {
            try {
                List<Event> events = eventService.getAllEvents();
                
                Platform.runLater(() -> {
                    eventSelectionView = createEventSelectionView(events);
                    showStep(Step.SELECT_EVENT);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Failed to load events");
                });
            }
        }).start();
    }
    
    private VBox createEventSelectionView(List<Event> events) {
        VBox view = new VBox(15);
        view.setPadding(new Insets(20));
        
        Label headerLabel = new Label("Select Event");
        headerLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        VBox eventsContainer = new VBox(15);
        eventsContainer.setPadding(new Insets(10));
        
        for (Event event : events) {
            if (!"CANCELLED".equals(event.getStatus())) {
                EventCard card = new EventCard(event);
                card.setOnMouseClicked(e -> {
                    selectedEvent = event;
                    nextButton.setDisable(false);
                    
                    // Highlight selected
                    eventsContainer.getChildren().forEach(node -> {
                        if (node instanceof EventCard) {
                            ((EventCard) node).setSelected(false);
                        }
                    });
                    card.setSelected(true);
                });
                eventsContainer.getChildren().add(card);
            }
        }
        
        scrollPane.setContent(eventsContainer);
        view.getChildren().addAll(headerLabel, scrollPane);
        
        return view;
    }
    
    private void showStep(Step step) {
        currentStep = step;
        contentArea.getChildren().clear();
        
        // Update step indicator
        stepIndicator.getChildren().clear();
        for (Step s : Step.values()) {
            VBox stepBox = createStepBox(s);
            stepIndicator.getChildren().add(stepBox);
            
            if (s != Step.CONFIRM) {
                Label arrow = new Label("→");
                arrow.setStyle(
                    "-fx-font-size: 18px;" +
                    "-fx-text-fill: rgba(255,255,255,0.5);"
                );
                stepIndicator.getChildren().add(arrow);
            }
        }
        
        // Update navigation buttons
        backButton.setDisable(step == Step.SELECT_EVENT);
        
        switch (step) {
            case SELECT_EVENT:
                contentArea.getChildren().add(eventSelectionView);
                nextButton.setText("Next →");
                nextButton.setDisable(selectedEvent == null);
                break;
                
            case CHOOSE_SECTION:
                sectionSelector = new EventSectionSelector(selectedEvent.getId());
                sectionSelector.setOnSectionSelected(section -> {
                    selectedSection = section;
                    nextButton.setDisable(false);
                });
                contentArea.getChildren().add(sectionSelector);
                nextButton.setText("Next →");
                nextButton.setDisable(true);
                break;
                
            case PICK_SEATS:
                if (selectedSection.getSectionType().equals("TRIBUNE")) {
                    seatMapGrid = new SeatMapGrid(
                        selectedEvent.getId(),
                        selectedSection.getSectionId(),
                        selectedSection.getSectionTitle(),
                        selectedSection.getPrice()
                    );
                    // Add listener to enable Next button when seats are selected
                    seatMapGrid.setOnSelectionChanged(() -> {
                        boolean hasSelection = !seatMapGrid.getSelectedSeats().isEmpty();
                        nextButton.setDisable(!hasSelection);
                    });
                    contentArea.getChildren().add(seatMapGrid);
                } else {
                    // Field/Standing tickets
                    VBox standingView = createStandingTicketView();
                    contentArea.getChildren().add(standingView);
                }
                nextButton.setText("Next →");
                nextButton.setDisable(true); // Start disabled until seats are selected
                break;
                
            case ENTER_DETAILS:
                customerForm = new CustomerFormPanel();
                contentArea.getChildren().add(customerForm);
                nextButton.setText("Review Booking →");
                nextButton.setDisable(false);
                break;
                
            case CONFIRM:
                if (seatMapGrid != null) {
                    selectedSeats = seatMapGrid.getSelectedSeatModels();
                }
                
                summaryCard = new BookingSummaryCard(
                    selectedEvent.getEventName(),
                    selectedSection.getSectionTitle(),
                    selectedSeats,
                    selectedSection.getPrice(),
                    customerForm.getCustomerName(),
                    customerForm.getCustomerEmail(),
                    customerForm.getCustomerPhone()
                );
                
                ScrollPane scrollPane = new ScrollPane(summaryCard);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: white;");
                scrollPane.setPadding(new Insets(20));
                
                contentArea.getChildren().add(scrollPane);
                nextButton.setText("✓ Confirm & Book");
                nextButton.setStyle(
                    "-fx-background-color: #4CAF50;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 10px 30px;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-cursor: hand;"
                );
                nextButton.setDisable(false);
                break;
        }
    }
    
    private VBox createStandingTicketView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(40));
        view.setAlignment(Pos.CENTER);
        
        Label icon = new Label();
        icon.setGraphic(IconUtil.createIcon(FontAwesomeIcon.TICKET, 64));
        icon.setStyle("-fx-text-fill: #2196F3;");
        
        Label titleLabel = new Label("Standing Tickets");
        titleLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Label descLabel = new Label("Select the number of standing tickets you want to purchase");
        descLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        // Quantity selector
        HBox quantityBox = new HBox(15);
        quantityBox.setAlignment(Pos.CENTER);
        quantityBox.setPadding(new Insets(20));
        
        Button minusBtn = new Button("−");
        minusBtn.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 45px;" +
            "-fx-min-height: 45px;" +
            "-fx-background-radius: 25px;" +
            "-fx-cursor: hand;"
        );
        
        Label quantityLabel = new Label("0");
        quantityLabel.setStyle(
            "-fx-font-size: 36px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;" +
            "-fx-min-width: 80px;" +
            "-fx-alignment: center;"
        );
        
        Button plusBtn = new Button("+");
        plusBtn.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-min-width: 45px;" +
            "-fx-min-height: 45px;" +
            "-fx-background-radius: 25px;" +
            "-fx-cursor: hand;"
        );
        
        minusBtn.setOnAction(e -> {
            int current = Integer.parseInt(quantityLabel.getText());
            if (current > 0) {
                current--;
                quantityLabel.setText(String.valueOf(current));
                updateStandingTickets(current);
            }
        });
        
        plusBtn.setOnAction(e -> {
            int current = Integer.parseInt(quantityLabel.getText());
            if (current < selectedSection.getAvailableSeats()) {
                current++;
                quantityLabel.setText(String.valueOf(current));
                updateStandingTickets(current);
            }
        });
        
        quantityBox.getChildren().addAll(minusBtn, quantityLabel, plusBtn);
        
        // Price display
        Label priceLabel = new Label("Rp " + String.format("%,.0f", selectedSection.getPrice()) + " per ticket");
        priceLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-text-fill: #2196F3;" +
            "-fx-font-weight: bold;"
        );
        
        Label availableLabel = new Label(selectedSection.getAvailableSeats() + " tickets available");
        availableLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        view.getChildren().addAll(icon, titleLabel, descLabel, quantityBox, priceLabel, availableLabel);
        return view;
    }
    
    private void updateStandingTickets(int quantity) {
        selectedSeats.clear();
        
        // Create dummy seats for standing tickets (no specific seat assignment)
        for (int i = 0; i < quantity; i++) {
            Seat standingSeat = new Seat();
            standingSeat.setSectionId(selectedSection.getSectionId());
            standingSeat.setStatus("AVAILABLE");
            selectedSeats.add(standingSeat);
        }
        
        nextButton.setDisable(quantity == 0);
    }
    
    private void nextStep() {
        if (currentStep == Step.ENTER_DETAILS) {
            // Validate customer form
            if (!customerForm.validate()) {
                return;
            }
        }
        
        if (currentStep == Step.PICK_SEATS && seatMapGrid != null) {
            selectedSeats = seatMapGrid.getSelectedSeatModels();
            if (selectedSeats.isEmpty()) {
                showError("Please select at least one seat");
                return;
            }
        }
        
        if (currentStep == Step.CONFIRM) {
            confirmBooking();
            return;
        }
        
        // Move to next step
        Step nextStep = Step.values()[currentStep.ordinal() + 1];
        showStep(nextStep);
    }
    
    private void previousStep() {
        if (currentStep.ordinal() > 0) {
            Step prevStep = Step.values()[currentStep.ordinal() - 1];
            showStep(prevStep);
        }
    }
    
    private void confirmBooking() {
        nextButton.setDisable(true);
        nextButton.setText("Processing...");
        
        new Thread(() -> {
            try {
                // Create booking object
                Booking booking = new Booking();
                booking.setEventId(selectedEvent.getId());
                booking.setCustomerName(customerForm.getCustomerName());
                booking.setCustomerEmail(customerForm.getCustomerEmail());
                booking.setCustomerPhone(customerForm.getCustomerPhone());
                booking.setTotalSeats(selectedSeats.size());
                booking.setTotalPrice(summaryCard.getTotalPrice());
                booking.setBookingStatus("CONFIRMED");
                
                // Create booking in database
                Booking savedBooking = bookingService.createBooking(booking, selectedSeats);
                
                Platform.runLater(() -> {
                    if (savedBooking != null) {
                        showSuccess(savedBooking);
                    } else {
                        showError("Failed to create booking. Please try again.");
                        nextButton.setDisable(false);
                        nextButton.setText("✓ Confirm & Book");
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("An error occurred: " + e.getMessage());
                    nextButton.setDisable(false);
                    nextButton.setText("✓ Confirm & Book");
                });
            }
        }).start();
    }
    
    private void showSuccess(Booking booking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Successful");
        alert.setHeaderText("Your booking has been confirmed!");
        alert.setContentText(
            "Booking Number: " + booking.getBookingNumber() + "\n" +
            "Event: " + selectedEvent.getEventName() + "\n" +
            "Seats: " + booking.getTotalSeats() + "\n" +
            "Total: Rp " + String.format("%,.0f", booking.getTotalPrice()) + "\n\n" +
            "A confirmation email will be sent to " + booking.getCustomerEmail()
        );
        alert.showAndWait();
        
        if (onBookingComplete != null) {
            onBookingComplete.run();
        }
        
        close();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Booking Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setOnBookingComplete(Runnable callback) {
        this.onBookingComplete = callback;
    }
    
    /**
     * Simple event card for selection
     */
    private static class EventCard extends HBox {
        private boolean selected = false;
        private final Event event;
        
        public EventCard(Event event) {
            this.event = event;
            initializeUI();
        }
        
        private void initializeUI() {
            setSpacing(15);
            setPadding(new Insets(15));
            setAlignment(Pos.CENTER_LEFT);
            setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"
            );
            
            setOnMouseEntered(e -> {
                if (!selected) {
                    setStyle(
                        "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #2196F3;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
                    );
                }
            });
            
            setOnMouseExited(e -> {
                if (!selected) {
                    setStyle(
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
                    );
                }
            });
            
            VBox info = new VBox(8);
            
            Label nameLabel = new Label(event.getEventName());
            nameLabel.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2c3e50;"
            );
            
            Label dateLabel = new Label(event.getEventDate().toString());
            dateLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #7f8c8d;"
            );
            
            info.getChildren().addAll(nameLabel, dateLabel);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label seatsLabel = new Label(event.getTotalSeats() + " seats");
            seatsLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #2196F3;" +
                "-fx-font-weight: bold;"
            );
            
            getChildren().addAll(info, spacer, seatsLabel);
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setStyle(
                    "-fx-background-color: #e3f2fd;" +
                    "-fx-border-color: #2196F3;" +
                    "-fx-border-width: 3px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #e0e0e0;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-cursor: hand;"
                );
            }
        }
    }
}
