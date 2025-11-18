package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.openjfx.model.Admin;

/**
 * Navigation Menu Component
 * Left sidebar with menu buttons and user info
 */
public class NavigationMenu extends VBox {
    
    private final Admin admin;
    private Button homeButton;
    private Button eventsButton;
    private Button seatsButton;
    private Button bookingsButton;
    private Button adminButton;      // NEW: Admin Management
    private Button staffButton;      // NEW: Staff Management
    private Button inventoryButton;  // NEW: Inventory Management
    private Button reportsButton;
    
    public NavigationMenu(Admin admin) {
        super(5);
        this.admin = admin;
        initialize();
    }
    
    private void initialize() {
        setPrefWidth(220);
        setStyle("-fx-background-color: #2c3e50;");
        setPadding(new Insets(20, 10, 20, 10));
        
        getChildren().addAll(
            createHeader(),
            createSeparator(),
            new Label(), // spacing
            createMenuButtons(),
            createLogoutButton()
        );
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label systemTitle = new Label("Stadium");
        systemTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        systemTitle.setStyle("-fx-text-fill: white;");
        
        Label systemSubtitle = new Label("Management System");
        systemSubtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        systemSubtitle.setStyle("-fx-text-fill: #bdc3c7;");
        
        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER);
        userBox.setPadding(new Insets(10, 0, 0, 0));
        
        FontAwesomeIconView userIcon = new FontAwesomeIconView(FontAwesomeIcon.USER);
        userIcon.setSize("14");
        userIcon.setFill(javafx.scene.paint.Color.WHITE);
        
        Label userLabel = new Label(admin.getUsername());
        userLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        userLabel.setStyle("-fx-text-fill: #ecf0f1;");
        
        userBox.getChildren().addAll(userIcon, userLabel);
        header.getChildren().addAll(systemTitle, systemSubtitle, userBox);
        
        return header;
    }
    
    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #34495e;");
        separator.setMaxWidth(180);
        return separator;
    }
    
    private VBox createMenuButtons() {
        VBox menuBox = new VBox(5);
        
        homeButton = createMenuButton("Dashboard", FontAwesomeIcon.HOME, true);
        eventsButton = createMenuButton("Events", FontAwesomeIcon.CALENDAR, false);
        seatsButton = createMenuButton("Stadium Config", FontAwesomeIcon.BUILDING, false);
        bookingsButton = createMenuButton("Bookings", FontAwesomeIcon.TICKET, false);
        
        // NEW: Management buttons (initially hidden)
        adminButton = createMenuButton("Admin Management", FontAwesomeIcon.USER_MD, false);
        staffButton = createMenuButton("Staff Management", FontAwesomeIcon.USERS, false);
        inventoryButton = createMenuButton("Inventory", FontAwesomeIcon.CUBES, false);
        reportsButton = createMenuButton("Reports", FontAwesomeIcon.BAR_CHART, false);
        
        // Hide management buttons by default
        adminButton.setVisible(false);
        adminButton.setManaged(false);
        staffButton.setVisible(false);
        staffButton.setManaged(false);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        menuBox.getChildren().addAll(
            homeButton, eventsButton, seatsButton, bookingsButton,
            adminButton, staffButton, inventoryButton, reportsButton,
            spacer
        );
        VBox.setVgrow(menuBox, Priority.ALWAYS);
        
        return menuBox;
    }
    
    private Button createLogoutButton() {
        Button logoutButton = createMenuButton("Logout", FontAwesomeIcon.SIGN_OUT, false);
        logoutButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                             "-fx-font-size: 13px; -fx-padding: 12px; " +
                             "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;");
        return logoutButton;
    }
    
    private Button createMenuButton(String text, FontAwesomeIcon icon, boolean isActive) {
        Button button = new Button();
        button.setPrefWidth(200);
        button.setMaxWidth(Double.MAX_VALUE);
        
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16");
        iconView.setStyle("-fx-fill: white;");
        
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        content.getChildren().addAll(iconView, textLabel);
        button.setGraphic(content);
        
        if (isActive) {
            button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                          "-fx-font-size: 13px; -fx-padding: 12px; " +
                          "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                          "-fx-font-size: 13px; -fx-padding: 12px; " +
                          "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;");
            
            button.setOnMouseEntered(e -> 
                button.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                              "-fx-font-size: 13px; -fx-padding: 12px; " +
                              "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;")
            );
            button.setOnMouseExited(e -> 
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                              "-fx-font-size: 13px; -fx-padding: 12px; " +
                              "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;")
            );
        }
        
        return button;
    }
    
    public void highlightButton(Button active) {
        Button[] allButtons = {homeButton, eventsButton, seatsButton, bookingsButton, 
                              adminButton, staffButton, inventoryButton, reportsButton};
        
        for (Button button : allButtons) {
            if (button == active) {
                button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                               "-fx-font-size: 13px; -fx-padding: 12px; " +
                               "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;");
            } else {
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                              "-fx-font-size: 13px; -fx-padding: 12px; " +
                              "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;");
                
                button.setOnMouseEntered(e -> 
                    button.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                                  "-fx-font-size: 13px; -fx-padding: 12px; " +
                                  "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;")
                );
                button.setOnMouseExited(e -> 
                    button.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                                  "-fx-font-size: 13px; -fx-padding: 12px; " +
                                  "-fx-background-radius: 5; -fx-cursor: hand; -fx-alignment: center-left;")
                );
            }
        }
    }
    
    /**
     * Setup role-based access control for menu buttons
     * @param role User role (ADMIN or SUPER_ADMIN)
     */
    public void setupRoles(String role) {
        // All users can access these features
        inventoryButton.setVisible(true);
        inventoryButton.setManaged(true);
        reportsButton.setVisible(true);
        reportsButton.setManaged(true);
        
        // Only SUPER_ADMIN can access admin and staff management
        if ("SUPER_ADMIN".equals(role)) {
            adminButton.setVisible(true);
            adminButton.setManaged(true);
            staffButton.setVisible(true);
            staffButton.setManaged(true);
            
            System.out.println("✓ SUPER_ADMIN privileges granted");
        } else {
            adminButton.setVisible(false);
            adminButton.setManaged(false);
            staffButton.setVisible(false);
            staffButton.setManaged(false);
            
            System.out.println("✓ ADMIN privileges granted");
        }
    }
    
    // Getters for buttons
    public Button getHomeButton() { return homeButton; }
    public Button getEventsButton() { return eventsButton; }
    public Button getSeatsButton() { return seatsButton; }
    public Button getBookingsButton() { return bookingsButton; }
    public Button getAdminButton() { return adminButton; }        // NEW
    public Button getStaffButton() { return staffButton; }        // NEW
    public Button getInventoryButton() { return inventoryButton; } // NEW
    public Button getReportsButton() { return reportsButton; }
    public Button getLogoutButton() { 
        return (Button) getChildren().get(getChildren().size() - 1); 
    }
}
