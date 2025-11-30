package org.openjfx.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.openjfx.component.DatabaseConfigDialog;
import org.openjfx.model.Admin;
import org.openjfx.service.AdminService;
import org.openjfx.util.IconUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

/**
 * Controller for the Login page
 */
public class LoginController {
    
    private Stage stage;
    private AdminService adminService;
    
    public LoginController(Stage stage) {
        this.stage = stage;
        this.adminService = new AdminService();
    }
    
    /**
     * Create and return the login scene
     */
    public Scene getScene() {
        // Create main container
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f4f4f4;");
        
        // Title
        Text titleText = new Text("Stadium Management System");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleText.setStyle("-fx-fill: #2c3e50;");
        
        Text subtitleText = new Text("Admin Login");
        subtitleText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitleText.setStyle("-fx-fill: #7f8c8d;");
        
        // Login form
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(30, 40, 30, 40));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        // Username field
        Label userLabel = new Label("Username:");
        userLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        grid.add(userLabel, 0, 0);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefWidth(250);
        usernameField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");
        grid.add(usernameField, 1, 0);
        
        // Password field
        Label passLabel = new Label("Password:");
        passLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        grid.add(passLabel, 0, 1);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefWidth(250);
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");
        grid.add(passwordField, 1, 1);
        
        // Message label for feedback
        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(350);
        grid.add(messageLabel, 0, 2, 2, 1);
        
        // Buttons
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(120);
        loginButton.setPrefHeight(35);
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-font-weight: bold; " +
                            "-fx-background-radius: 5; -fx-cursor: hand;");
        
        // Hover effect for login button
        loginButton.setOnMouseEntered(e -> 
            loginButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                                "-fx-background-radius: 5; -fx-cursor: hand;")
        );
        loginButton.setOnMouseExited(e -> 
            loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                                "-fx-background-radius: 5; -fx-cursor: hand;")
        );
        
        // Database settings button
        Button dbConfigButton = new Button("Database Settings");
        dbConfigButton.setGraphic(IconUtil.createIcon(FontAwesomeIcon.COG, 14));
        dbConfigButton.setPrefWidth(160);
        dbConfigButton.setPrefHeight(35);
        dbConfigButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                               "-fx-font-size: 13px; -fx-font-weight: bold; " +
                               "-fx-background-radius: 5; -fx-cursor: hand;");
        
        // Hover effect for db config button
        dbConfigButton.setOnMouseEntered(e -> 
            dbConfigButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; " +
                                   "-fx-font-size: 13px; -fx-font-weight: bold; " +
                                   "-fx-background-radius: 5; -fx-cursor: hand;")
        );
        dbConfigButton.setOnMouseExited(e -> 
            dbConfigButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                                   "-fx-font-size: 13px; -fx-font-weight: bold; " +
                                   "-fx-background-radius: 5; -fx-cursor: hand;")
        );
        dbConfigButton.setOnAction(e -> openDatabaseConfig());
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(dbConfigButton, loginButton);
        grid.add(buttonBox, 0, 3, 2, 1);
        
        // Login button action
        loginButton.setOnAction(e -> handleLogin(usernameField, passwordField, messageLabel));
        
        // Allow Enter key to submit
        passwordField.setOnAction(e -> handleLogin(usernameField, passwordField, messageLabel));
        
        // Add all to root
        root.getChildren().addAll(titleText, subtitleText, grid);
        
        Scene scene = new Scene(root, 600, 500);
        return scene;
    }
    
    /**
     * Handle login button click
     */
    private void handleLogin(TextField usernameField, PasswordField passwordField, Label messageLabel) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showMessage(messageLabel, "Please enter both username and password.", "error");
            return;
        }
        
        // Authenticate
        try {
            Admin admin = adminService.authenticate(username, password);
            
            if (admin != null) {
                showMessage(messageLabel, "Login successful! Welcome, " + admin.getUsername(), "success");
                System.out.println("Admin logged in: " + admin);
                
                // Navigate to dashboard after a short delay
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(800);
                        showDashboard(admin);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });
                
            } else {
                showMessage(messageLabel, "Invalid username or password.", "error");
                passwordField.clear();
            }
        } catch (Exception ex) {
            showMessage(messageLabel, "Error connecting to database. Please check your connection.", "error");
            System.err.println("Login error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Show message to user
     */
    private void showMessage(Label label, String message, String type) {
        label.setText(message);
        if (type.equals("error")) {
            label.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if (type.equals("success")) {
            label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            label.setStyle("-fx-text-fill: #34495e;");
        }
    }
    
    /**
     * Open database configuration dialog
     */
    private void openDatabaseConfig() {
        DatabaseConfigDialog configDialog = new DatabaseConfigDialog(stage);
        configDialog.showAndWait();
        
        // If configuration was saved, reload the .env
        if (configDialog.isConfigSaved()) {
            try {
                // Reload dotenv
                io.github.cdimascio.dotenv.Dotenv.configure().ignoreIfMissing().load();
                System.out.println("Database configuration reloaded successfully");
            } catch (Exception e) {
                System.err.println("Error reloading configuration: " + e.getMessage());
            }
        }
    }
    
    /**
     * Show dashboard
     */
    private void showDashboard(Admin admin) {
        DashboardController dashboardController = new DashboardController(stage, admin);
        Scene dashboardScene = dashboardController.getScene();
        
        // Enable resizing for dashboard (it was disabled for login)
        stage.setResizable(true);
        stage.setScene(dashboardScene);
        stage.setTitle("Stadium Management System - Dashboard");
    }
}
