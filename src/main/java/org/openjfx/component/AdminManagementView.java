package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.openjfx.model.Admin;
import org.openjfx.service.AdminService;

import java.util.List;
import java.util.Optional;

/**
 * Complete Admin Management View with TableView
 * Only accessible by SUPER_ADMIN
 */
public class AdminManagementView extends VBox {
    
    private final AdminService adminService;
    private TableView<Admin> adminTable;
    private ObservableList<Admin> adminList;
    private Admin currentAdmin;
    private Label totalLabel;
    private Label superAdminLabel;
    private Label regularAdminLabel;
    
    public AdminManagementView(Admin currentAdmin) {
        this.adminService = new AdminService();
        this.currentAdmin = currentAdmin;
        this.adminList = FXCollections.observableArrayList();
        
        setupUI();
        loadAdmins();
    }
    
    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #ecf0f1;");
        
        // Header
        VBox header = createHeader();
        
        // Statistics Cards
        HBox statsCards = createStatsCards();
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        // Table Container
        VBox tableContainer = createTableContainer();
        
        // Add all components
        getChildren().addAll(header, statsCards, toolbar, tableContainer);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(8);
        
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.USER_SECRET);
        icon.setSize("32");
        icon.setGlyphStyle("-fx-fill: #2c3e50;");
        
        Label titleLabel = new Label("Admin Management");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        titleBox.getChildren().addAll(icon, titleLabel);
        
        Label descLabel = new Label("Manage admin users and their roles • Only SUPER_ADMIN can access this page");
        descLabel.setFont(Font.font("Arial", 13));
        descLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        header.getChildren().addAll(titleBox, descLabel);
        return header;
    }
    
    private HBox createStatsCards() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Total Admins Card
        VBox totalCard = createStatCard("Total Admins", "0", "#3498db", FontAwesomeIcon.USERS);
        totalLabel = (Label) totalCard.lookup("#stat-value");
        
        // Super Admins Card
        VBox superCard = createStatCard("Super Admins", "0", "#e74c3c", FontAwesomeIcon.STAR);
        superAdminLabel = (Label) superCard.lookup("#stat-value");
        
        // Regular Admins Card
        VBox regularCard = createStatCard("Regular Admins", "0", "#27ae60", FontAwesomeIcon.USER);
        regularAdminLabel = (Label) regularCard.lookup("#stat-value");
        
        statsBox.getChildren().addAll(totalCard, superCard, regularCard);
        return statsBox;
    }
    
    private VBox createStatCard(String title, String value, String color, FontAwesomeIcon iconType) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        card.setPrefWidth(240);
        card.setPrefHeight(110);
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("20");
        icon.setGlyphStyle("-fx-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        titleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        headerBox.getChildren().addAll(icon, titleLabel);
        
        Label valueLabel = new Label(value);
        valueLabel.setId("stat-value");
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(headerBox, valueLabel);
        return card;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(15));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        
        Button addButton = new Button("Add New Admin");
        FontAwesomeIconView addIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        addIcon.setSize("14");
        addIcon.setGlyphStyle("-fx-fill: white;");
        addButton.setGraphic(addIcon);
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                          "-fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 13px;");
        addButton.setOnAction(e -> showAddAdminDialog());
        
        Button refreshButton = new Button("Refresh");
        FontAwesomeIconView refreshIcon = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        refreshIcon.setSize("14");
        refreshIcon.setGlyphStyle("-fx-fill: white;");
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                              "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> loadAdmins());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label currentUserLabel = new Label("Logged in as: " + currentAdmin.getUsername() + " (" + currentAdmin.getRole() + ")");
        currentUserLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        currentUserLabel.setStyle("-fx-text-fill: #34495e; -fx-background-color: #ecf0f1; " +
                                 "-fx-padding: 8 15; -fx-background-radius: 5;");
        
        toolbar.getChildren().addAll(addButton, refreshButton, spacer, currentUserLabel);
        return toolbar;
    }
    
    private VBox createTableContainer() {
        VBox container = new VBox();
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        container.setPadding(new Insets(20));
        
        adminTable = createAdminTable();
        
        container.getChildren().add(adminTable);
        VBox.setVgrow(adminTable, Priority.ALWAYS);
        
        return container;
    }
    
    private TableView<Admin> createAdminTable() {
        TableView<Admin> table = new TableView<>();
        table.setItems(adminList);
        table.setStyle("-fx-background-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // ID Column
        TableColumn<Admin, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);
        idCol.setStyle("-fx-alignment: CENTER;");
        
        // Username Column
        TableColumn<Admin, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(200);
        usernameCol.setCellFactory(col -> new TableCell<Admin, String>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);
                if (empty || username == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    FontAwesomeIconView userIcon = new FontAwesomeIconView(FontAwesomeIcon.USER_CIRCLE);
                    userIcon.setSize("16");
                    userIcon.setGlyphStyle("-fx-fill: #3498db;");
                    
                    
                    Label nameLabel = new Label(username);
                    nameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
                    
                    box.getChildren().addAll(userIcon, nameLabel);
                    setGraphic(box);
                }
            }
        });
        
        // Role Column with badges
        TableColumn<Admin, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(150);
        roleCol.setCellFactory(col -> new TableCell<Admin, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label badge = new Label(role);
                    badge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                    badge.setPadding(new Insets(5, 12, 5, 12));
                    badge.setStyle("-fx-background-radius: 12;");
                    
                    if ("SUPER_ADMIN".equals(role)) {
                        badge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                     "-fx-background-radius: 12; -fx-font-weight: bold;");
                        
                        HBox box = new HBox(5);
                        box.setAlignment(Pos.CENTER_LEFT);
                        FontAwesomeIconView starIcon = new FontAwesomeIconView(FontAwesomeIcon.STAR);
                        starIcon.setSize("12");
                        starIcon.setGlyphStyle("-fx-fill: white;");
                        badge.setGraphic(starIcon);
                        
                        setGraphic(badge);
                    } else {
                        badge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                     "-fx-background-radius: 12; -fx-font-weight: bold;");
                        setGraphic(badge);
                    }
                }
            }
        });
        
        // Created Info Column (placeholder)
        TableColumn<Admin, String> infoCol = new TableColumn<>("Info");
        infoCol.setPrefWidth(150);
        infoCol.setCellFactory(col -> new TableCell<Admin, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Admin admin = getTableView().getItems().get(getIndex());
                    if (admin.getId() == currentAdmin.getId()) {
                        Label youLabel = new Label("(You)");
                        youLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        setGraphic(youLabel);
                    } else {
                        setText("Active");
                        setStyle("-fx-text-fill: #7f8c8d;");
                    }
                }
            }
        });
        
        // Actions Column
        TableColumn<Admin, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(220);
        actionsCol.setCellFactory(col -> new TableCell<Admin, Void>() {
            private final Button editButton = new Button("Edit Role");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonsBox = new HBox(8, editButton, deleteButton);
            
            {
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
                editIcon.setSize("12");
                editIcon.setGlyphStyle("-fx-fill: white;");
                editButton.setGraphic(editIcon);
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                                   "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
                
                FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                deleteIcon.setSize("12");
                deleteIcon.setGlyphStyle("-fx-fill: white;");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                     "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
                
                editButton.setOnAction(e -> {
                    Admin admin = getTableView().getItems().get(getIndex());
                    showEditAdminDialog(admin);
                });
                
                deleteButton.setOnAction(e -> {
                    Admin admin = getTableView().getItems().get(getIndex());
                    handleDeleteAdmin(admin);
                });
                
                buttonsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Admin admin = getTableView().getItems().get(getIndex());
                    // Can't delete yourself
                    boolean isSelf = admin.getId() == currentAdmin.getId();
                    deleteButton.setDisable(isSelf);
                    editButton.setDisable(isSelf);
                    
                    if (isSelf) {
                        deleteButton.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; " +
                                            "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 4;");
                        editButton.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; " +
                                          "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 4;");
                    }
                    setGraphic(buttonsBox);
                }
            }
        });
        
        table.getColumns().addAll(idCol, usernameCol, roleCol, infoCol, actionsCol);
        
        // Empty state
        table.setPlaceholder(new Label("No admins found. Click 'Add New Admin' to create one."));
        
        return table;
    }
    
    private void loadAdmins() {
        List<Admin> admins = adminService.getAllAdmins();
        adminList.clear();
        adminList.addAll(admins);
        updateStats();
    }
    
    private void updateStats() {
        long superAdminCount = adminList.stream().filter(a -> "SUPER_ADMIN".equals(a.getRole())).count();
        long regularAdminCount = adminList.stream().filter(a -> "ADMIN".equals(a.getRole())).count();
        
        if (totalLabel != null) totalLabel.setText(String.valueOf(adminList.size()));
        if (superAdminLabel != null) superAdminLabel.setText(String.valueOf(superAdminCount));
        if (regularAdminLabel != null) regularAdminLabel.setText(String.valueOf(regularAdminCount));
    }
    
    private void showAddAdminDialog() {
        Dialog<Admin> dialog = new Dialog<>();
        dialog.setTitle("Add New Admin");
        dialog.setHeaderText("Create a new administrator account");
        
        // Set icon
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        
        ButtonType createButtonType = new ButtonType("Create Admin", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");
        confirmPasswordField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("ADMIN", "SUPER_ADMIN");
        roleComboBox.setValue("ADMIN");
        roleComboBox.setStyle("-fx-font-size: 13px;");
        
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        
        Label confirmLabel = new Label("Confirm Password:");
        confirmLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        
        Label roleLabel = new Label("Role:");
        roleLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        
        Label roleHint = new Label("ADMIN: Limited access • SUPER_ADMIN: Full access");
        roleHint.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(confirmLabel, 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(roleLabel, 0, 3);
        grid.add(roleComboBox, 1, 3);
        grid.add(roleHint, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        
        // Enable/disable create button
        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);
        
        usernameField.textProperty().addListener((obs, old, newVal) -> {
            createButton.setDisable(newVal.trim().isEmpty() || passwordField.getText().isEmpty());
        });
        passwordField.textProperty().addListener((obs, old, newVal) -> {
            createButton.setDisable(newVal.trim().isEmpty() || usernameField.getText().trim().isEmpty());
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert("Password Mismatch", "Passwords do not match! Please try again.", Alert.AlertType.ERROR);
                    return null;
                }
                
                if (passwordField.getText().length() < 4) {
                    showAlert("Weak Password", "Password must be at least 4 characters long.", Alert.AlertType.WARNING);
                    return null;
                }
                
                Admin newAdmin = new Admin();
                newAdmin.setUsername(usernameField.getText().trim());
                newAdmin.setPassword(passwordField.getText());
                newAdmin.setRole(roleComboBox.getValue());
                return newAdmin;
            }
            return null;
        });
        
        Optional<Admin> result = dialog.showAndWait();
        result.ifPresent(admin -> {
            if (adminService.createAdmin(admin.getUsername(), admin.getPassword(), admin.getRole())) {
                showAlert("Success", "Admin '" + admin.getUsername() + "' created successfully!", Alert.AlertType.INFORMATION);
                loadAdmins();
            } else {
                showAlert("Error", "Failed to create admin. Username might already exist.", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void showEditAdminDialog(Admin admin) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Admin Role");
        dialog.setHeaderText("Edit role for: " + admin.getUsername());
        
        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        Label currentLabel = new Label("Current Role: " + admin.getRole());
        currentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        currentLabel.setStyle("-fx-text-fill: #34495e;");
        
        Label newLabel = new Label("New Role:");
        newLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("ADMIN", "SUPER_ADMIN");
        roleComboBox.setValue(admin.getRole());
        roleComboBox.setPrefWidth(200);
        roleComboBox.setStyle("-fx-font-size: 13px;");
        
        Label warning = new Label("⚠ Changing role will affect access permissions");
        warning.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 11px;");
        
        content.getChildren().addAll(currentLabel, new Separator(), newLabel, roleComboBox, warning);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return roleComboBox.getValue();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            if (adminService.updateAdminRole(admin.getId(), newRole)) {
                showAlert("Success", "Admin role updated successfully!", Alert.AlertType.INFORMATION);
                loadAdmins();
            } else {
                showAlert("Error", "Failed to update admin role.", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void handleDeleteAdmin(Admin admin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Admin");
        alert.setHeaderText("Delete admin: " + admin.getUsername() + "?");
        alert.setContentText("This action cannot be undone. The admin account will be permanently deleted.");
        
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            if (adminService.deleteAdmin(admin.getId())) {
                showAlert("Success", "Admin deleted successfully!", Alert.AlertType.INFORMATION);
                loadAdmins();
            } else {
                showAlert("Error", "Failed to delete admin.", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
