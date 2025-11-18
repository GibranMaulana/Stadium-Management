package org.openjfx.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.openjfx.component.*;
import org.openjfx.model.Admin;
import org.openjfx.model.Event;
import org.openjfx.service.EventService;

import java.util.List;

/**
 * Main Dashboard Controller with Navigation
 * Refactored to use component-based architecture
 */
public class DashboardController {
    
    private final Stage stage;
    private final Admin admin;
    private final EventService eventService;
    
    private BorderPane mainLayout;
    private NavigationMenu navigationMenu;
    private StackPane contentArea;
    
    public DashboardController(Stage stage, Admin admin) {
        this.stage = stage;
        this.admin = admin;
        this.eventService = new EventService();
    }
    
    /**
     * Create and return the dashboard scene
     */
    public Scene getScene() {
        mainLayout = new BorderPane();
        
        // Create navigation menu (left sidebar)
        navigationMenu = new NavigationMenu(admin);
        setupNavigationHandlers();
        mainLayout.setLeft(navigationMenu);
        
        // Create content area (center)
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #ecf0f1;");
        mainLayout.setCenter(contentArea);
        
        // Show home page by default
        showHomePage();
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        
        // Configure stage for better cross-platform compatibility (especially Windows)
        stage.setResizable(true);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setMaximized(true);
        
        return scene;
    }
    
    /**
     * Setup navigation button handlers
     */
    private void setupNavigationHandlers() {
        navigationMenu.getHomeButton().setOnAction(e -> {
            navigationMenu.highlightButton(navigationMenu.getHomeButton());
            showHomePage();
        });
        
        navigationMenu.getEventsButton().setOnAction(e -> {
            navigationMenu.highlightButton(navigationMenu.getEventsButton());
            showEventsPage();
        });
        
        navigationMenu.getSeatsButton().setOnAction(e -> {
            navigationMenu.highlightButton(navigationMenu.getSeatsButton());
            showSeatsPage();
        });
        
        navigationMenu.getBookingsButton().setOnAction(e -> {
            navigationMenu.highlightButton(navigationMenu.getBookingsButton());
            showBookingsPage();
        });
        
        navigationMenu.getReportsButton().setOnAction(e -> {
            navigationMenu.highlightButton(navigationMenu.getReportsButton());
            showReportsPage();
        });
        
        navigationMenu.getLogoutButton().setOnAction(e -> handleLogout());
    }
    
    /**
     * Show home/dashboard page
     */
    private void showHomePage() {
        VBox homePage = new VBox(30);
        homePage.setPadding(new Insets(40));
        homePage.setAlignment(Pos.TOP_LEFT);
        
        // Page title
        Label titleLabel = new Label("Dashboard Overview");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label welcomeLabel = new Label("Welcome back, " + admin.getUsername() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        welcomeLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        // Statistics cards with real data
        StatsSection statsSection = new StatsSection();
        
        // Quick actions section
        HBox quickActionsBar = createQuickActionsBar();
        
        // Recent activity section
        Label recentLabel = new Label("Recent Events");
        recentLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 18));
        recentLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        VBox activityBox = new VBox(10);
        activityBox.setPadding(new Insets(20));
        activityBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        EventCardList eventCardList = new EventCardList();
        activityBox.getChildren().add(eventCardList);
        
        homePage.getChildren().addAll(titleLabel, welcomeLabel, statsSection, quickActionsBar, recentLabel, activityBox);
        
        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(homePage);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #ecf0f1; -fx-background-color: #ecf0f1;");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }
    
    /**
     * Create quick actions bar with booking button
     */
    private HBox createQuickActionsBar() {
        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        actionsBar.setPadding(new Insets(20));
        actionsBar.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );
        
        Label actionsLabel = new Label("Quick Actions");
        actionsLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Book Tickets button
        Button bookButton = new Button("Book Tickets");
        bookButton.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.4), 5, 0, 0, 2);"
        );
        
        FontAwesomeIconView bookIcon = new FontAwesomeIconView(FontAwesomeIcon.TICKET);
        bookIcon.setSize("16");
        bookIcon.setFill(javafx.scene.paint.Color.WHITE);
        bookButton.setGraphic(bookIcon);
        
        bookButton.setOnAction(e -> openBookingWizard());
        
        // Hover effect
        bookButton.setOnMouseEntered(e -> {
            bookButton.setStyle(
                "-fx-background-color: #1976D2;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 12px 24px;" +
                "-fx-background-radius: 5px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.6), 8, 0, 0, 3);"
            );
        });
        
        bookButton.setOnMouseExited(e -> {
            bookButton.setStyle(
                "-fx-background-color: #2196F3;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 12px 24px;" +
                "-fx-background-radius: 5px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.4), 5, 0, 0, 2);"
            );
        });
        
        // Create Event button
        Button createEventButton = new Button("Create Event");
        createEventButton.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        
        FontAwesomeIconView createIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        createIcon.setSize("16");
        createIcon.setFill(javafx.scene.paint.Color.WHITE);
        createEventButton.setGraphic(createIcon);
        
        createEventButton.setOnAction(e -> showCreateEventDialog());
        
        actionsBar.getChildren().addAll(actionsLabel, spacer, bookButton, createEventButton);
        return actionsBar;
    }
    
    /**
     * Open the booking wizard in the main content area
     */
    private void openBookingWizard() {
        BookingWizardView wizardView = new BookingWizardView();
        wizardView.setOnBookingComplete(() -> {
            // Go back to home page after successful booking
            navigationMenu.highlightButton(navigationMenu.getHomeButton());
            showHomePage();
        });
        wizardView.setOnCancel(() -> {
            // Go back to home page on cancel
            navigationMenu.highlightButton(navigationMenu.getHomeButton());
            showHomePage();
        });
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(wizardView);
    }
    
    /**
     * Show events page
     */
    private void showEventsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(30));
        page.setStyle("-fx-background-color: #f5f5f5;");

        // Title and controls bar
        HBox titleBar = createTitleBar();

        // Search and filter bar
        EventFilterBar filterBar = new EventFilterBar();

        // Events table
        EventTableView eventsTable = new EventTableView(
            this::showEditEventDialog,
            this::showDeleteConfirmation
        );

        // Load initial data
        loadEventsData(eventsTable);

        // Event handlers for filters
        setupFilterHandlers(filterBar, eventsTable);

        VBox.setVgrow(eventsTable, Priority.ALWAYS);
        page.getChildren().addAll(titleBar, filterBar, eventsTable);
        
        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #f5f5f5; -fx-background-color: #f5f5f5;");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }
    
    private HBox createTitleBar() {
        HBox titleBar = new HBox(20);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Events Management");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = new Button("Create Event");
        createBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; " +
                          "-fx-padding: 10 20; -fx-cursor: hand;");
        FontAwesomeIconView createIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        createIcon.setSize("16");
        createIcon.setFill(javafx.scene.paint.Color.WHITE);
        createBtn.setGraphic(createIcon);
        createBtn.setOnAction(e -> showCreateEventDialog());

        titleBar.getChildren().addAll(title, spacer, createBtn);
        return titleBar;
    }
    
    private void setupFilterHandlers(EventFilterBar filterBar, EventTableView eventsTable) {
        filterBar.getSearchField().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadEventsData(eventsTable);
            } else {
                filterEvents(eventsTable, newVal, 
                           filterBar.getTypeFilter().getValue(), 
                           filterBar.getStatusFilter().getValue());
            }
        });

        filterBar.getTypeFilter().setOnAction(e -> 
            filterEvents(eventsTable, 
                       filterBar.getSearchField().getText(), 
                       filterBar.getTypeFilter().getValue(), 
                       filterBar.getStatusFilter().getValue()));

        filterBar.getStatusFilter().setOnAction(e -> 
            filterEvents(eventsTable, 
                       filterBar.getSearchField().getText(), 
                       filterBar.getTypeFilter().getValue(), 
                       filterBar.getStatusFilter().getValue()));

        filterBar.getRefreshBtn().setOnAction(e -> {
            filterBar.reset();
            loadEventsData(eventsTable);
        });
    }

    private void loadEventsData(EventTableView table) {
        table.loadEvents(eventService.getAllEvents());
    }

    private void filterEvents(EventTableView table, String searchTerm, String type, String status) {
        List<Event> events;

        // Start with all events or search results
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            events = eventService.searchEvents(searchTerm);
        } else {
            events = eventService.getAllEvents();
        }

        // Apply type filter
        if (type != null && !type.equals("All Types")) {
            events = events.stream()
                .filter(e -> e.getEventType().equals(type))
                .collect(java.util.stream.Collectors.toList());
        }

        // Apply status filter
        if (status != null && !status.equals("All Status")) {
            events = events.stream()
                .filter(e -> e.getStatus().equals(status))
                .collect(java.util.stream.Collectors.toList());
        }

        table.loadEvents(events);
    }

    private void showCreateEventDialog() {
        // Show unified event form view
        EventFormView eventFormView = new EventFormView(null, this::showEventsPage);
        
        ScrollPane scrollPane = new ScrollPane(eventFormView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private void showEditEventDialog(Event event) {
        // Show unified event form view for editing (includes section configuration)
        EventFormView eventFormView = new EventFormView(event, this::showEventsPage);
        
        ScrollPane scrollPane = new ScrollPane(eventFormView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private void showDeleteConfirmation(Event event) {
        new DeleteConfirmationDialog(stage, event, this::showEventsPage).show();
    }
    
    /**
     * Show stadium configuration page
     */
    private void showSeatsPage() {
        StadiumConfigView stadiumConfigView = new StadiumConfigView();
        
        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(stadiumConfigView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #ecf0f1; -fx-background-color: #ecf0f1;");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }
    
    /**
     * Show bookings management page
     */
    private void showBookingsPage() {
        BookingsManagementView bookingsView = new BookingsManagementView();
        
        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(bookingsView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #ecf0f1; -fx-background-color: #ecf0f1;");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }
    
    /**
     * Show reports page (placeholder)
     */
    private void showReportsPage() {
        VBox reportsPage = new VBox(20);
        reportsPage.setPadding(new Insets(40));
        reportsPage.setAlignment(Pos.TOP_CENTER);
        
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.BAR_CHART);
        icon.setSize("28");
        icon.setStyle("-fx-fill: #2c3e50;");
        
        Label titleLabel = new Label("Reports & Analytics");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        titleBox.getChildren().addAll(icon, titleLabel);
        
        Label infoLabel = new Label("Reports and analytics page will be implemented here.\nView sales reports, event statistics, and revenue analytics.");
        infoLabel.setFont(Font.font("Arial", 14));
        infoLabel.setStyle("-fx-text-fill: #7f8c8d;");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(600);
        
        reportsPage.getChildren().addAll(titleBox, infoLabel);
        
        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(reportsPage);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #ecf0f1; -fx-background-color: #ecf0f1;");
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }
    
    /**
     * Handle logout
     */
    private void handleLogout() {
        LoginController loginController = new LoginController(stage);
        stage.setScene(loginController.getScene());
        stage.setTitle("Stadium Management System - Login");
    }
}
