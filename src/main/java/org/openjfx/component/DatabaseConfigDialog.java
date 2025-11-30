package org.openjfx.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import org.openjfx.util.IconUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog for configuring database connection settings
 * Allows users to set SSMS connection parameters
 */
public class DatabaseConfigDialog extends Stage {
    
    private TextField hostField;
    private TextField portField;
    private TextField databaseField;
    private TextField usernameField;
    private PasswordField passwordField;
    private CheckBox encryptCheckBox;
    private Label statusLabel;
    private Button saveButton;
    private Button testButton;
    private Button setupDbButton;
    
    private boolean configSaved = false;
    
    public DatabaseConfigDialog(Stage owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Database Configuration");
        setResizable(true);
        setMinWidth(450);
        setMinHeight(400);
        
        createUI();
        loadCurrentConfig();
    }
    
    private void createUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        VBox header = createHeader();
        
        // Form area wrapped in ScrollPane
        VBox formArea = createFormArea();
        ScrollPane scrollPane = new ScrollPane(formArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Footer with buttons
        HBox footer = createFooter();
        
        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(footer);
        
        Scene scene = new Scene(root, 500, 550);
        setScene(scene);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2196F3;");
        
        Label titleLabel = new Label("Database Configuration");
        titleLabel.setGraphic(IconUtil.createIcon(FontAwesomeIcon.DATABASE, 24));
        titleLabel.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-graphic-text-gap: 10px;"
        );
        
        Label subtitleLabel = new Label(
            "Configure SQL Server connection settings.\n" +
            "These settings will be saved to .env file."
        );
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: rgba(255,255,255,0.9);"
        );
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createFormArea() {
        VBox formArea = new VBox(15);
        formArea.setPadding(new Insets(25));
        formArea.setStyle("-fx-background-color: white;");
        
        // Host
        Label hostLabel = new Label("Database Host:");
        hostLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        hostField = new TextField();
        hostField.setPromptText("e.g., localhost or 192.168.1.100");
        hostField.setStyle(createInputStyle());
        
        // Port
        Label portLabel = new Label("Port:");
        portLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        portField = new TextField();
        portField.setPromptText("e.g., 1433");
        portField.setStyle(createInputStyle());
        
        // Database Name
        Label dbLabel = new Label("Database Name:");
        dbLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        databaseField = new TextField();
        databaseField.setPromptText("e.g., StadiumDB");
        databaseField.setStyle(createInputStyle());
        
        // Username
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        usernameField = new TextField();
        usernameField.setPromptText("e.g., sa");
        usernameField.setStyle(createInputStyle());
        
        // Password
        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Database password");
        passwordField.setStyle(createInputStyle());
        
        // Encryption
        encryptCheckBox = new CheckBox("Enable SSL/TLS Encryption");
        encryptCheckBox.setStyle("-fx-font-size: 13px;");
        
        // Info box
        HBox infoBox = new HBox(10);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle(
            "-fx-background-color: #E3F2FD;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #2196F3;" +
            "-fx-border-radius: 5px;" +
            "-fx-border-width: 1px;"
        );
        
        Label infoIcon = new Label();
        infoIcon.setGraphic(IconUtil.createIcon(FontAwesomeIcon.INFO_CIRCLE, 16));
        infoIcon.setStyle("-fx-text-fill: #2196F3;");
        
        Label infoText = new Label(
            "Tip: Use 'localhost' for local SQL Server. " +
            "Click 'Test Connection' to verify settings before saving."
        );
        infoText.setWrapText(true);
        infoText.setStyle("-fx-font-size: 11px; -fx-text-fill: #1976D2;");
        HBox.setHgrow(infoText, Priority.ALWAYS);
        
        infoBox.getChildren().addAll(infoIcon, infoText);
        
        // Status label
        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setVisible(false);
        statusLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-padding: 10px;" +
            "-fx-background-radius: 5px;"
        );
        
        formArea.getChildren().addAll(
            hostLabel, hostField,
            portLabel, portField,
            dbLabel, databaseField,
            userLabel, usernameField,
            passLabel, passwordField,
            encryptCheckBox,
            infoBox,
            statusLabel
        );
        
        return formArea;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 20, 15, 20));
        footer.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        testButton = new Button("Test Connection");
        testButton.setGraphic(IconUtil.createIcon(FontAwesomeIcon.PLUG, 14));
        testButton.setStyle(
            "-fx-background-color: #FF9800;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        testButton.setOnAction(e -> testConnection());
        
        setupDbButton = new Button("Setup Database");
        setupDbButton.setGraphic(IconUtil.createIcon(FontAwesomeIcon.COG, 14));
        setupDbButton.setStyle(
            "-fx-background-color: #9C27B0;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        setupDbButton.setOnAction(e -> setupDatabase());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle(
            "-fx-background-color: #6c757d;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        cancelButton.setOnAction(e -> close());
        
        saveButton = new Button("Save Configuration");
        saveButton.setGraphic(IconUtil.createIcon(FontAwesomeIcon.SAVE, 14));
        saveButton.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10px 30px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        saveButton.setOnAction(e -> saveConfiguration());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        footer.getChildren().addAll(setupDbButton, spacer, testButton, cancelButton, saveButton);
        return footer;
    }
    
    private java.nio.file.Path getConfigFilePath() {
        // Try current directory first (for development)
        java.nio.file.Path localPath = Paths.get(".env");
        if (Files.exists(localPath) && Files.isWritable(localPath.getParent())) {
            return localPath;
        }
        
        // Use user's home directory for production (installed apps)
        String userHome = System.getProperty("user.home");
        java.nio.file.Path appDataPath = Paths.get(userHome, ".stadium-management");
        
        try {
            // Create directory if it doesn't exist
            if (!Files.exists(appDataPath)) {
                Files.createDirectories(appDataPath);
            }
        } catch (Exception e) {
            System.err.println("Failed to create config directory: " + e.getMessage());
        }
        
        return appDataPath.resolve(".env");
    }
    
    private void loadCurrentConfig() {
        try {
            // Load from .env file
            java.nio.file.Path envPath = getConfigFilePath();
            if (Files.exists(envPath)) {
                Map<String, String> config = parseEnvFile(envPath);
                
                hostField.setText(config.getOrDefault("DB_HOST", "localhost"));
                portField.setText(config.getOrDefault("DB_PORT", "1433"));
                databaseField.setText(config.getOrDefault("DB_NAME", "StadiumDB"));
                usernameField.setText(config.getOrDefault("DB_USER", "sa"));
                passwordField.setText(config.getOrDefault("DB_PASSWORD", ""));
                encryptCheckBox.setSelected(
                    "true".equalsIgnoreCase(config.getOrDefault("DB_ENCRYPT", "false"))
                );
            } else {
                // Set defaults
                hostField.setText("localhost");
                portField.setText("1433");
                databaseField.setText("StadiumDB");
                usernameField.setText("sa");
                encryptCheckBox.setSelected(false);
            }
        } catch (Exception e) {
            showStatus("Error loading configuration: " + e.getMessage(), false);
        }
    }
    
    private Map<String, String> parseEnvFile(java.nio.file.Path path) throws IOException {
        Map<String, String> config = new HashMap<>();
        for (String line : Files.readAllLines(path)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            int equalsIndex = line.indexOf('=');
            if (equalsIndex > 0) {
                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();
                config.put(key, value);
            }
        }
        return config;
    }
    
    private void testConnection() {
        if (!validateInput()) return;
        
        testButton.setDisable(true);
        testButton.setText("Testing...");
        statusLabel.setVisible(false);
        
        new Thread(() -> {
            try {
                String host = hostField.getText().trim();
                String port = portField.getText().trim();
                String database = databaseField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String encrypt = encryptCheckBox.isSelected() ? "true" : "false";
                
                String connectionUrl = String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=true",
                    host, port, database, encrypt
                );
                
                try (Connection conn = java.sql.DriverManager.getConnection(
                    connectionUrl, username, password
                )) {
                    javafx.application.Platform.runLater(() -> {
                        showStatus("✓ Connection successful!", true);
                        testButton.setDisable(false);
                        testButton.setText("Test Connection");
                    });
                }
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showStatus("✗ Connection failed: " + e.getMessage(), false);
                    testButton.setDisable(false);
                    testButton.setText("Test Connection");
                });
            }
        }).start();
    }
    
    private void saveConfiguration() {
        if (!validateInput()) return;
        
        saveButton.setDisable(true);
        saveButton.setText("Saving...");
        
        try {
            String envContent = buildEnvFileContent();
            java.nio.file.Path envPath = getConfigFilePath();
            
            // Ensure parent directory exists
            java.nio.file.Path parentDir = envPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Write using Files API
            Files.write(envPath, envContent.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.WRITE);
            
            System.out.println("Configuration saved to: " + envPath.toAbsolutePath());
            
            showStatus("✓ Configuration saved successfully!", true);
            configSaved = true;
            
            // Wait a moment then close
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> close());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (IOException e) {
            showStatus("✗ Failed to save configuration: " + e.getMessage(), false);
            saveButton.setDisable(false);
            saveButton.setText("Save Configuration");
        } catch (Exception e) {
            showStatus("✗ Failed to save: " + e.getMessage(), false);
            saveButton.setDisable(false);
            saveButton.setText("Save Configuration");
        }
    }
    
    private String buildEnvFileContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Stadium Management System - Environment Configuration\n");
        sb.append("# This file contains your local database credentials\n");
        sb.append("# ⚠️ NEVER commit this file to Git! (already in .gitignore)\n\n");
        
        sb.append("# Database Configuration\n");
        sb.append("DB_HOST=").append(hostField.getText().trim()).append("\n");
        sb.append("DB_PORT=").append(portField.getText().trim()).append("\n");
        sb.append("DB_NAME=").append(databaseField.getText().trim()).append("\n");
        sb.append("DB_USER=").append(usernameField.getText().trim()).append("\n");
        sb.append("DB_PASSWORD=").append(passwordField.getText()).append("\n");
        sb.append("DB_ENCRYPT=").append(encryptCheckBox.isSelected()).append("\n\n");
        
        sb.append("# Application Configuration\n");
        sb.append("APP_NAME=Stadium Management System\n");
        sb.append("APP_VERSION=1.0.0\n\n");
        
        sb.append("# Email Configuration (Gmail)\n");
        sb.append("# To use Gmail, you need to create an App Password:\n");
        sb.append("# 1. Go to https://myaccount.google.com/apppasswords\n");
        sb.append("# 2. Generate a new app password for \"Mail\"\n");
        sb.append("# 3. Use that 16-character password below (not your regular Gmail password)\n");
        sb.append("EMAIL_USERNAME=narbiganaluam@gmail.com\n");
        sb.append("EMAIL_PASSWORD=wqyvclpuebhiwwuv\n");
        sb.append("EMAIL_FROM=noreply@stadiummanagement.com\n");
        
        return sb.toString();
    }
    
    private boolean validateInput() {
        if (hostField.getText().trim().isEmpty()) {
            showStatus("✗ Database host is required", false);
            hostField.requestFocus();
            return false;
        }
        
        if (portField.getText().trim().isEmpty()) {
            showStatus("✗ Port is required", false);
            portField.requestFocus();
            return false;
        }
        
        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65535) {
                showStatus("✗ Port must be between 1 and 65535", false);
                portField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showStatus("✗ Port must be a valid number", false);
            portField.requestFocus();
            return false;
        }
        
        if (databaseField.getText().trim().isEmpty()) {
            showStatus("✗ Database name is required", false);
            databaseField.requestFocus();
            return false;
        }
        
        if (usernameField.getText().trim().isEmpty()) {
            showStatus("✗ Username is required", false);
            usernameField.requestFocus();
            return false;
        }
        
        if (passwordField.getText().isEmpty()) {
            showStatus("✗ Password is required", false);
            passwordField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-padding: 10px;" +
            "-fx-background-radius: 5px;" +
            "-fx-background-color: " + (success ? "#D4EDDA" : "#F8D7DA") + ";" +
            "-fx-text-fill: " + (success ? "#155724" : "#721c24") + ";" +
            "-fx-border-color: " + (success ? "#C3E6CB" : "#F5C6CB") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 5px;"
        );
        statusLabel.setVisible(true);
    }
    
    private void setupDatabase() {
        if (!validateInput()) return;
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Setup Database");
        confirmDialog.setHeaderText("Initialize Database Schema");
        confirmDialog.setContentText(
            "This will create the StadiumDB database and all required tables.\\n\\n" +
            "⚠️ Warning: If the database already exists, some operations may fail.\\n\\n" +
            "Continue with database setup?"
        );
        
        if (confirmDialog.showAndWait().orElse(null) != javafx.scene.control.ButtonType.OK) {
            return;
        }
        
        setupDbButton.setDisable(true);
        setupDbButton.setText("Setting up...");
        statusLabel.setVisible(false);
        
        new Thread(() -> {
            try {
                String host = hostField.getText().trim();
                String port = portField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String encrypt = encryptCheckBox.isSelected() ? "true" : "false";
                
                // SQL files to execute in order
                String[] sqlFiles = {
                    "/database/01_initial_setup.sql",
                    "/database/02_sync_seats.sql",
                    "/database/03_features_roles_staff_inventory.sql",
                    "/database/04_add_inventory_fields.sql",
                    "/database/06_event_expenses.sql",
                    "/database/07_allow_null_seatid_for_standing_areas.sql"
                };
                
                StringBuilder result = new StringBuilder();
                int successCount = 0;
                
                for (int i = 0; i < sqlFiles.length; i++) {
                    String sqlFile = sqlFiles[i];
                    String fileName = sqlFile.substring(sqlFile.lastIndexOf('/') + 1);
                    
                    javafx.application.Platform.runLater(() -> 
                        statusLabel.setText("Executing: " + fileName + "...")
                    );
                    
                    try {
                        String sqlContent = readResourceFile(sqlFile);
                        
                        // For first file, connect without database; for others, use StadiumDB
                        String dbName = (i == 0) ? "master" : databaseField.getText().trim();
                        String connectionUrl = String.format(
                            "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=true",
                            host, port, dbName, encrypt
                        );
                        
                        executeSqlScript(connectionUrl, username, password, sqlContent);
                        successCount++;
                        result.append("✓ ").append(fileName).append("\\n");
                        
                    } catch (Exception e) {
                        result.append("✗ ").append(fileName).append(": ").append(e.getMessage()).append("\\n");
                    }
                }
                
                final int finalSuccessCount = successCount;
                javafx.application.Platform.runLater(() -> {
                    setupDbButton.setDisable(false);
                    setupDbButton.setText("Setup Database");
                    
                    if (finalSuccessCount == sqlFiles.length) {
                        showStatus("✓ Database setup completed successfully!", true);
                    } else {
                        showStatus("⚠ Setup completed with some errors ("+finalSuccessCount+"/"+sqlFiles.length+")", false);
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showStatus("✗ Database setup failed: " + e.getMessage(), false);
                    setupDbButton.setDisable(false);
                    setupDbButton.setText("Setup Database");
                });
            }
        }).start();
    }
    
    private String readResourceFile(String resourcePath) throws IOException {
        try (java.io.InputStream is = getClass().getResourceAsStream(resourcePath);
             java.io.BufferedReader reader = new java.io.BufferedReader(
                 new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
            
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\\n");
            }
            return content.toString();
        }
    }
    
    private void executeSqlScript(String connectionUrl, String username, String password, String sqlScript) throws Exception {
        try (Connection conn = java.sql.DriverManager.getConnection(connectionUrl, username, password);
             java.sql.Statement stmt = conn.createStatement()) {
            
            // Split by GO statement (SQL Server batch separator)
            String[] batches = sqlScript.split("(?i)\\\\bGO\\\\b");
            
            for (String batch : batches) {
                String trimmedBatch = batch.trim();
                if (!trimmedBatch.isEmpty() && !trimmedBatch.startsWith("--")) {
                    try {
                        stmt.execute(trimmedBatch);
                    } catch (Exception e) {
                        // Log but continue (some statements may fail if already exist)
                        System.err.println("Warning: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private String createInputStyle() {
        return "-fx-font-size: 13px;" +
               "-fx-padding: 8px 12px;" +
               "-fx-background-radius: 5px;" +
               "-fx-border-color: #ced4da;" +
               "-fx-border-radius: 5px;" +
               "-fx-border-width: 1px;";
    }
    
    public boolean isConfigSaved() {
        return configSaved;
    }
}
