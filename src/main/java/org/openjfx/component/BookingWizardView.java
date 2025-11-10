package org.openjfx.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.openjfx.model.Booking;
import org.openjfx.model.Event;
import org.openjfx.model.EventSection;
import org.openjfx.model.Seat;
import org.openjfx.service.BookingService;
import org.openjfx.service.EventService;
import org.openjfx.util.IconUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking Wizard as an embedded view (not a dialog)
 * Steps: Select Event → Choose Section → Pick Seats → Enter Details → Confirm
 */
public class BookingWizardView extends VBox {
    
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
    private Spinner<Integer> quantitySpinner;
    
    // Data
    private Event selectedEvent;
    private EventSection selectedSection;
    private List<Seat> selectedSeats;
    
    // Services
    private final EventService eventService;
    private final BookingService bookingService;
    
    // Callbacks
    private Runnable onBookingComplete;
    private Runnable onCancel;
    
    public BookingWizardView() {
        this.eventService = new EventService();
        this.bookingService = new BookingService();
        this.selectedSeats = new ArrayList<>();
        
        initializeUI();
        loadEvents();
    }
    
    private void initializeUI() {
        setSpacing(0);
        setStyle("-fx-background-color: #f5f5f5;");
        
        // Header with step indicator
        VBox header = createHeader();
        
        // Content area (scrollable)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        contentArea = new VBox(20);
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle("-fx-background-color: white;");
        
        scrollPane.setContent(contentArea);
        
        // Navigation buttons footer
        HBox footer = createNavigationButtons();
        
        getChildren().addAll(header, scrollPane, footer);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #2196F3;");
        
        Label titleLabel = new Label("Book Your Tickets");
        titleLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        
        // Step indicator
        stepIndicator = new HBox(10);
        stepIndicator.setAlignment(Pos.CENTER);
        
        header.getChildren().addAll(titleLabel, stepIndicator);
        return header;
    }
    
    private HBox createStepBox(Step step) {
        VBox stepBox = new VBox(5);
        stepBox.setAlignment(Pos.CENTER);
        stepBox.setPadding(new Insets(10));
        stepBox.setMinWidth(120);
        
        boolean isActive = step == currentStep;
        boolean isCompleted = step.number < currentStep.number;
        
        // Step number circle
        Label numberLabel = new Label(String.valueOf(step.number));
        numberLabel.setAlignment(Pos.CENTER);
        numberLabel.setMinSize(35, 35);
        numberLabel.setMaxSize(35, 35);
        numberLabel.setStyle(
            "-fx-background-color: " + (isCompleted ? "#4CAF50" : isActive ? "white" : "rgba(255,255,255,0.3)") + ";" +
            "-fx-background-radius: 50%;" +
            "-fx-text-fill: " + (isActive && !isCompleted ? "#2196F3" : "white") + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );
        
        // Step title
        Label titleLabel = new Label(step.title);
        titleLabel.setStyle(
            "-fx-text-fill: " + (isActive ? "white" : "rgba(255,255,255,0.7)") + ";" +
            "-fx-font-size: " + (isActive ? "13px" : "11px") + ";" +
            "-fx-font-weight: " + (isActive ? "bold" : "normal") + ";"
        );
        
        stepBox.getChildren().addAll(numberLabel, titleLabel);
        return new HBox(stepBox);
    }
    
    private HBox createNavigationButtons() {
        HBox nav = new HBox(15);
        nav.setAlignment(Pos.CENTER_RIGHT);
        nav.setPadding(new Insets(20, 30, 20, 30));
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
        cancelButton.setOnAction(e -> handleCancel());
        
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
    
    private void handleCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
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
        
        Label headerLabel = new Label("Select an Event");
        headerLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        VBox eventsContainer = new VBox(15);
        
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
        
        view.getChildren().addAll(headerLabel, eventsContainer);
        return view;
    }
    
    private void showStep(Step step) {
        currentStep = step;
        contentArea.getChildren().clear();
        
        // Update step indicator
        stepIndicator.getChildren().clear();
        for (Step s : Step.values()) {
            HBox stepBox = createStepBox(s);
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
                nextButton.setDisable(true);
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
                
                contentArea.getChildren().add(summaryCard);
                nextButton.setText("✓ Confirm & Book");
                nextButton.setDisable(false);
                break;
        }
    }
    
    private VBox createStandingTicketView() {
        VBox view = new VBox(20);
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(30));
        
        Label titleLabel = new Label(selectedSection.getSectionTitle() + " - Standing Tickets");
        titleLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Label infoLabel = new Label("Select the number of tickets you want to purchase");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        HBox quantityBox = new HBox(15);
        quantityBox.setAlignment(Pos.CENTER);
        
        Label qtyLabel = new Label("Number of Tickets:");
        qtyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        quantitySpinner = new Spinner<>(1, selectedSection.getAvailableSeats(), 1);
        quantitySpinner.setPrefWidth(100);
        quantitySpinner.valueProperty().addListener((obs, old, newVal) -> {
            nextButton.setDisable(newVal == null || newVal <= 0);
            updateStandingTicketPrice(newVal);
        });
        
        quantityBox.getChildren().addAll(qtyLabel, quantitySpinner);
        
        Label priceLabel = new Label("Total Price: Rp " + String.format("%,.0f", selectedSection.getPrice()));
        priceLabel.setId("standingPriceLabel");
        priceLabel.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2196F3;"
        );
        
        view.getChildren().addAll(titleLabel, infoLabel, quantityBox, priceLabel);
        return view;
    }
    
    private void updateStandingTicketPrice(int quantity) {
        double total = quantity * selectedSection.getPrice();
        Label priceLabel = (Label) contentArea.lookup("#standingPriceLabel");
        if (priceLabel != null) {
            priceLabel.setText("Total Price: Rp " + String.format("%,.0f", total));
        }
    }
    
    private void previousStep() {
        Step prevStep = currentStep;
        
        if (currentStep == Step.CHOOSE_SECTION) {
            prevStep = Step.SELECT_EVENT;
        } else if (currentStep == Step.PICK_SEATS) {
            prevStep = Step.CHOOSE_SECTION;
        } else if (currentStep == Step.ENTER_DETAILS) {
            prevStep = Step.PICK_SEATS;
        } else if (currentStep == Step.CONFIRM) {
            prevStep = Step.ENTER_DETAILS;
        }
        
        if (prevStep != currentStep) {
            showStep(prevStep);
        }
    }
    
    private void nextStep() {
        switch (currentStep) {
            case SELECT_EVENT:
                // Validate that the event has sections configured
                new Thread(() -> {
                    org.openjfx.service.EventSectionService ess = new org.openjfx.service.EventSectionService();
                    java.util.List<org.openjfx.model.EventSection> sections = ess.getEventSections(selectedEvent.getId());
                    
                    Platform.runLater(() -> {
                        if (sections.isEmpty()) {
                            showError(
                                "This event has no sections configured!\n\n" +
                                "Event: " + selectedEvent.getEventName() + "\n\n" +
                                "Please contact the administrator to configure " +
                                "sections, pricing, and capacity for this event before booking."
                            );
                        } else {
                            showStep(Step.CHOOSE_SECTION);
                        }
                    });
                }).start();
                break;
            case CHOOSE_SECTION:
                showStep(Step.PICK_SEATS);
                break;
            case PICK_SEATS:
                if (selectedSection.getSectionType().equals("FIELD")) {
                    createStandingTicketSeats();
                }
                showStep(Step.ENTER_DETAILS);
                break;
            case ENTER_DETAILS:
                if (customerForm.validate()) {
                    showStep(Step.CONFIRM);
                }
                break;
            case CONFIRM:
                confirmBooking();
                break;
        }
    }
    
    private void createStandingTicketSeats() {
        selectedSeats = new ArrayList<>();
        int quantity = quantitySpinner.getValue();
        
        for (int i = 0; i < quantity; i++) {
            Seat standingSeat = new Seat(0, selectedSection.getSectionId(), null, 0, "AVAILABLE");
            selectedSeats.add(standingSeat);
        }
    }
    
    private void confirmBooking() {
        nextButton.setDisable(true);
        nextButton.setText("Processing...");
        
        new Thread(() -> {
            try {
                Booking booking = new Booking();
                booking.setEventId(selectedEvent.getId());
                booking.setCustomerName(customerForm.getCustomerName());
                booking.setCustomerEmail(customerForm.getCustomerEmail());
                booking.setCustomerPhone(customerForm.getCustomerPhone());
                booking.setTotalSeats(selectedSeats.size());
                booking.setTotalPrice(selectedSeats.size() * selectedSection.getPrice());
                booking.setBookingStatus("CONFIRMED");
                
                Booking result = bookingService.createBooking(booking, selectedSeats);
                
                Platform.runLater(() -> {
                    if (result != null) {
                        showSuccessDialog(result.getBookingNumber());
                    } else {
                        showError("Failed to create booking. Please try again.");
                        nextButton.setDisable(false);
                        nextButton.setText("✓ Confirm & Book");
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Error creating booking: " + e.getMessage());
                    nextButton.setDisable(false);
                    nextButton.setText("✓ Confirm & Book");
                });
            }
        }).start();
    }
    
    private void showSuccessDialog(String bookingNumber) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Successful");
        alert.setHeaderText("Your booking has been confirmed!");
        alert.setContentText(
            "Booking Number: " + bookingNumber + "\n\n" +
            "A confirmation email will be sent to " + customerForm.getCustomerEmail() + "\n\n" +
            "Please save your booking number for reference."
        );
        alert.showAndWait();
        
        if (onBookingComplete != null) {
            onBookingComplete.run();
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setOnBookingComplete(Runnable callback) {
        this.onBookingComplete = callback;
    }
    
    public void setOnCancel(Runnable callback) {
        this.onCancel = callback;
    }
    
    // Inner class for event card
    private class EventCard extends VBox {
        private boolean selected = false;
        private final Event event;
        private boolean hasSections = true; // Assume true initially
        
        public EventCard(Event event) {
            this.event = event;
            
            setPadding(new Insets(15));
            setSpacing(8);
            setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dee2e6;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"
            );
            
            Label nameLabel = new Label(event.getEventName());
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            Label detailsLabel = new Label(
                event.getEventType() + " • " +
                event.getEventDate() + " • " +
                event.getEventTime()
            );
            detailsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
            
            getChildren().addAll(nameLabel, detailsLabel);
            
            // Check if event has sections configured and show warning if not
            new Thread(() -> {
                org.openjfx.service.EventSectionService ess = new org.openjfx.service.EventSectionService();
                java.util.List<org.openjfx.model.EventSection> sections = ess.getEventSections(event.getId());
                
                Platform.runLater(() -> {
                    if (sections.isEmpty()) {
                        hasSections = false;
                        Label warningLabel = new Label("⚠ Sections not configured");
                        warningLabel.setStyle(
                            "-fx-font-size: 11px;" +
                            "-fx-text-fill: #FF9800;" +
                            "-fx-background-color: #FFF3E0;" +
                            "-fx-padding: 4px 8px;" +
                            "-fx-background-radius: 4px;"
                        );
                        getChildren().add(warningLabel);
                    }
                });
            }).start();
            
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
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
                    );
                }
            });
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
                    "-fx-border-color: #dee2e6;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-cursor: hand;"
                );
            }
        }
    }
}
