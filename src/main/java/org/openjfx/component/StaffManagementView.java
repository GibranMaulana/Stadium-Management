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
import org.openjfx.model.Staff;
import org.openjfx.service.StaffService;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Complete Staff Management View with TableView
 * Accessible by SUPER_ADMIN only
 */
public class StaffManagementView extends VBox {
    
    private final StaffService staffService;
    private ObservableList<Staff> staffList;
    private final NumberFormat currencyFormat;
    private final DateTimeFormatter dateFormatter;
    
    private Label activeCountLabel;
    private Label totalSalaryLabel;
    private Label avgSalaryLabel;
    private CheckBox showInactiveCheck;
    private ScrollPane cardsScrollPane; // Store reference to update cards
    
    public StaffManagementView() {
        this.staffService = new StaffService();
        this.staffList = FXCollections.observableArrayList();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        this.dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        
        setupUI();
        loadStaff();
    }
    
    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa 0%, #c3cfe2 100%);");
        
        // Header
        VBox header = createHeader();
        
        // Statistics Cards
        HBox statsCards = createStatsCards();
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        // Table Container
        cardsScrollPane = createTableContainer();
        
        // Add all components
        getChildren().addAll(header, statsCards, toolbar, cardsScrollPane);
        VBox.setVgrow(cardsScrollPane, Priority.ALWAYS);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(8);
        
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.USERS);
        icon.setSize("32");
        icon.setGlyphStyle("-fx-fill: #2c3e50;");
        
        Label titleLabel = new Label("Staff Management");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        titleBox.getChildren().addAll(icon, titleLabel);
        
        Label descLabel = new Label("Manage staff members, positions, salaries, and employment records");
        descLabel.setFont(Font.font("Arial", 13));
        descLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        header.getChildren().addAll(titleBox, descLabel);
        return header;
    }
    
    private HBox createStatsCards() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Active Staff Card
        VBox activeCard = createStatCard("Active Staff", "0", "#27ae60", FontAwesomeIcon.USER_PLUS);
        activeCountLabel = (Label) activeCard.lookup("#stat-value");
        
        // Total Salary Card
        VBox salaryCard = createStatCard("Total Monthly Salary", "Rp 0", "#e74c3c", FontAwesomeIcon.MONEY);
        totalSalaryLabel = (Label) salaryCard.lookup("#stat-value");
        
        // Average Salary Card
        VBox avgSalaryCard = createStatCard("Average Salary", "Rp 0", "#3498db", FontAwesomeIcon.CALCULATOR);
        avgSalaryLabel = (Label) avgSalaryCard.lookup("#stat-value");
        
        statsBox.getChildren().addAll(activeCard, salaryCard, avgSalaryCard);
        return statsBox;
    }
    
    private VBox createStatCard(String title, String value, String color, FontAwesomeIcon iconType) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        card.setPrefWidth(260);
        card.setPrefHeight(110);
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("20");
        icon.setGlyphStyle("-fx-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        titleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        headerBox.getChildren().addAll(icon, titleLabel);
        
        Label valueLabel = new Label(value);
        valueLabel.setId("stat-value");
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        valueLabel.setWrapText(true);
        
        card.getChildren().addAll(headerBox, valueLabel);
        return card;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(15));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        
        Button addButton = new Button("Add New Staff");
        FontAwesomeIconView addIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        addIcon.setSize("14");
        addIcon.setGlyphStyle("-fx-fill: white;");
        addButton.setGraphic(addIcon);
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        addButton.setOnAction(e -> showAddStaffDialog());
        
        Button refreshButton = new Button("Refresh");
        FontAwesomeIconView refreshIcon = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        refreshIcon.setSize("14");
        refreshIcon.setGlyphStyle("-fx-fill: white;");
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                              "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> loadStaff());
        
        showInactiveCheck = new CheckBox("Show Inactive Staff");
        showInactiveCheck.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        showInactiveCheck.setStyle("-fx-text-fill: #34495e;");
        showInactiveCheck.setOnAction(e -> filterStaff());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label countLabel = new Label();
        countLabel.setId("count-label");
        countLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        countLabel.setStyle("-fx-text-fill: #34495e; -fx-background-color: #ecf0f1; " +
                           "-fx-padding: 8 15; -fx-background-radius: 5;");
        
        toolbar.getChildren().addAll(addButton, refreshButton, showInactiveCheck, spacer, countLabel);
        return toolbar;
    }
    
    private ScrollPane createTableContainer() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        FlowPane cardsGrid = new FlowPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(20));
        cardsGrid.setStyle("-fx-background-color: transparent;");
        
        for (Staff staff : staffList) {
            StaffCard card = new StaffCard(
                staff,
                () -> showViewDialog(staff),
                () -> showEditDialog(staff),
                () -> toggleStaffStatus(staff)
            );
            cardsGrid.getChildren().add(card);
        }
        
        scrollPane.setContent(cardsGrid);
        return scrollPane;
    }
    
    private TableView<Staff> createStaffTable() {
        TableView<Staff> table = new TableView<>();
        table.setItems(staffList);
        table.setStyle("-fx-background-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // ID Column
        TableColumn<Staff, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");
        
        // Name Column with icon
        TableColumn<Staff, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(180);
        nameCol.setCellFactory(col -> new TableCell<Staff, String>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    FontAwesomeIconView userIcon = new FontAwesomeIconView(FontAwesomeIcon.USER_CIRCLE_ALT);
                    userIcon.setSize("16");
                    userIcon.setGlyphStyle("-fx-fill: #3498db;");
                    
                    Label nameLabel = new Label(name);
                    nameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
                    
                    box.getChildren().addAll(userIcon, nameLabel);
                    setGraphic(box);
                }
            }
        });
        
        // Position Column with badge
        TableColumn<Staff, String> positionCol = new TableColumn<>("Position");
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        positionCol.setPrefWidth(160);
        positionCol.setCellFactory(col -> new TableCell<Staff, String>() {
            @Override
            protected void updateItem(String position, boolean empty) {
                super.updateItem(position, empty);
                if (empty || position == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(position);
                    badge.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 11));
                    badge.setPadding(new Insets(4, 10, 4, 10));
                    badge.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #34495e; -fx-background-radius: 10;");
                    setGraphic(badge);
                }
            }
        });
        
        // Salary Column (formatted)
        TableColumn<Staff, Double> salaryCol = new TableColumn<>("Monthly Salary");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        salaryCol.setPrefWidth(130);
        salaryCol.setCellFactory(col -> new TableCell<Staff, Double>() {
            @Override
            protected void updateItem(Double salary, boolean empty) {
                super.updateItem(salary, empty);
                if (empty || salary == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(salary));
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    setStyle("-fx-text-fill: #27ae60;");
                }
            }
        });
        
        // Phone Column
        TableColumn<Staff, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(120);
        phoneCol.setCellFactory(col -> new TableCell<Staff, String>() {
            @Override
            protected void updateItem(String phone, boolean empty) {
                super.updateItem(phone, empty);
                if (empty || phone == null) {
                    setText(null);
                } else {
                    setText(phone);
                    setStyle("-fx-text-fill: #7f8c8d;");
                }
            }
        });
        
        // Hire Date Column
        TableColumn<Staff, LocalDate> hireDateCol = new TableColumn<>("Hired On");
        hireDateCol.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        hireDateCol.setPrefWidth(110);
        hireDateCol.setCellFactory(col -> new TableCell<Staff, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                    setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                }
            }
        });
        
        // Status Column with colored badge
        TableColumn<Staff, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setPrefWidth(90);
        statusCol.setCellFactory(col -> new TableCell<Staff, Boolean>() {
            @Override
            protected void updateItem(Boolean isActive, boolean empty) {
                super.updateItem(isActive, empty);
                if (empty || isActive == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusBadge = new Label(isActive ? "● Active" : "● Inactive");
                    statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                    statusBadge.setPadding(new Insets(5, 10, 5, 10));
                    statusBadge.setStyle(isActive ? 
                        "-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-background-radius: 12;" :
                        "-fx-text-fill: #95a5a6; -fx-background-color: #ecf0f1; -fx-background-radius: 12;");
                    setGraphic(statusBadge);
                }
            }
        });
        
        // Actions Column
        TableColumn<Staff, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(240);
        actionsCol.setCellFactory(col -> new TableCell<Staff, Void>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button toggleButton = new Button();
            private final HBox buttonsBox = new HBox(6, viewButton, editButton, toggleButton);
            
            {
                // View Button
                FontAwesomeIconView viewIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                viewIcon.setSize("12");
                viewIcon.setGlyphStyle("-fx-fill: white;");
                viewButton.setGraphic(viewIcon);
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                   "-fx-font-size: 10px; -fx-padding: 5 10; -fx-background-radius: 4; -fx-cursor: hand;");
                
                // Edit Button
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
                editIcon.setSize("12");
                editIcon.setGlyphStyle("-fx-fill: white;");
                editButton.setGraphic(editIcon);
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                                   "-fx-font-size: 10px; -fx-padding: 5 10; -fx-background-radius: 4; -fx-cursor: hand;");
                
                viewButton.setOnAction(e -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    showStaffDetailsDialog(staff);
                });
                
                editButton.setOnAction(e -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    showEditStaffDialog(staff);
                });
                
                toggleButton.setOnAction(e -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    handleToggleStaffStatus(staff);
                });
                
                buttonsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Staff staff = getTableView().getItems().get(getIndex());
                    
                    if (staff.isActive()) {
                        toggleButton.setText("Deactivate");
                        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.BAN);
                        icon.setSize("12");
                        icon.setGlyphStyle("-fx-fill: white;");
                        toggleButton.setGraphic(icon);
                        toggleButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                            "-fx-font-size: 10px; -fx-padding: 5 10; -fx-background-radius: 4; -fx-cursor: hand;");
                    } else {
                        toggleButton.setText("Activate");
                        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
                        icon.setSize("12");
                        icon.setGlyphStyle("-fx-fill: white;");
                        toggleButton.setGraphic(icon);
                        toggleButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                            "-fx-font-size: 10px; -fx-padding: 5 10; -fx-background-radius: 4; -fx-cursor: hand;");
                    }
                    
                    setGraphic(buttonsBox);
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, positionCol, salaryCol, phoneCol, hireDateCol, statusCol, actionsCol);
        
        // Empty state
        table.setPlaceholder(new Label("No staff members found. Click 'Add New Staff' to add one."));
        
        return table;
    }
    
    private void loadStaff() {
        List<Staff> allStaff = staffService.getAllStaff();
        staffList.clear();
        
        if (showInactiveCheck != null && !showInactiveCheck.isSelected()) {
            allStaff.removeIf(s -> !s.isActive());
        }
        
        staffList.addAll(allStaff);
        updateStats();
        updateCountLabel();
        refreshCardsDisplay(); // Rebuild UI cards after data loaded
    }
    
    private void filterStaff() {
        loadStaff();
    }
    
    private void updateStats() {
        long activeCount = staffList.stream().filter(Staff::isActive).count();
        double totalSalary = staffList.stream().filter(Staff::isActive).mapToDouble(Staff::getSalary).sum();
        double avgSalary = activeCount > 0 ? totalSalary / activeCount : 0;
        
        if (activeCountLabel != null) activeCountLabel.setText(String.valueOf(activeCount));
        if (totalSalaryLabel != null) {
            totalSalaryLabel.setText(currencyFormat.format(totalSalary));
            totalSalaryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        }
        if (avgSalaryLabel != null) {
            avgSalaryLabel.setText(currencyFormat.format(avgSalary));
            avgSalaryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        }
    }
    
    private void updateCountLabel() {
        Label countLabel = (Label) lookup("#count-label");
        if (countLabel != null) {
            long activeCount = staffList.stream().filter(Staff::isActive).count();
            countLabel.setText("Showing " + staffList.size() + " staff (" + activeCount + " active)");
        }
    }
    
    private void showStaffDetailsDialog(Staff staff) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Staff Details");
        dialog.setHeaderText("Staff Information: " + staff.getFullName());
        
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");
        
        int row = 0;
        
        addDetailRow(grid, row++, "ID:", String.valueOf(staff.getStaffId()));
        addDetailRow(grid, row++, "Full Name:", staff.getFullName());
        addDetailRow(grid, row++, "Position:", staff.getPosition());
        addDetailRow(grid, row++, "Monthly Salary:", currencyFormat.format(staff.getSalary()));
        addDetailRow(grid, row++, "Phone:", staff.getPhoneNumber());
        addDetailRow(grid, row++, "Address:", staff.getAddress());
        addDetailRow(grid, row++, "Hire Date:", staff.getHireDate().format(dateFormatter));
        addDetailRow(grid, row++, "Status:", staff.isActive() ? "Active" : "Inactive");
        
        // Calculate employment duration
        long months = java.time.temporal.ChronoUnit.MONTHS.between(staff.getHireDate(), LocalDate.now());
        long years = months / 12;
        long remainingMonths = months % 12;
        String duration = years > 0 ? years + " years, " + remainingMonths + " months" : months + " months";
        addDetailRow(grid, row++, "Employment Duration:", duration);
        
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }
    
    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        labelNode.setStyle("-fx-text-fill: #7f8c8d;");
        
        Label valueNode = new Label(value);
        valueNode.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        valueNode.setStyle("-fx-text-fill: #2c3e50;");
        valueNode.setWrapText(true);
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
    
    private void showAddStaffDialog() {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Add New Staff");
        dialog.setHeaderText("Add a new staff member to the team");
        
        ButtonType addButtonType = new ButtonType("Add Staff", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("e.g., John Doe");
        nameField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        ComboBox<String> positionCombo = new ComboBox<>();
        positionCombo.getItems().addAll(
            "Security Manager", "Cleaning Supervisor", "Ticketing Staff", 
            "Medical Officer", "Maintenance Head", "Event Coordinator",
            "Cashier", "Customer Service", "IT Support", "Other"
        );
        positionCombo.setPromptText("Select position");
        positionCombo.setEditable(true);
        positionCombo.setStyle("-fx-font-size: 13px;");
        
        TextField salaryField = new TextField();
        salaryField.setPromptText("e.g., 5000000");
        salaryField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g., 081234567890");
        phoneField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Enter full address");
        addressArea.setPrefRowCount(3);
        addressArea.setWrapText(true);
        addressArea.setStyle("-fx-font-size: 13px;");
        
        DatePicker hireDatePicker = new DatePicker(LocalDate.now());
        hireDatePicker.setStyle("-fx-font-size: 13px;");
        
        grid.add(createLabel("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Position:"), 0, 1);
        grid.add(positionCombo, 1, 1);
        grid.add(createLabel("Monthly Salary (Rp):"), 0, 2);
        grid.add(salaryField, 1, 2);
        grid.add(createLabel("Phone Number:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(createLabel("Address:"), 0, 4);
        grid.add(addressArea, 1, 4);
        grid.add(createLabel("Hire Date:"), 0, 5);
        grid.add(hireDatePicker, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/disable add button
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        nameField.textProperty().addListener((obs, old, newVal) -> {
            addButton.setDisable(newVal.trim().isEmpty() || salaryField.getText().isEmpty());
        });
        salaryField.textProperty().addListener((obs, old, newVal) -> {
            addButton.setDisable(newVal.trim().isEmpty() || nameField.getText().trim().isEmpty());
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Staff newStaff = new Staff();
                    newStaff.setFullName(nameField.getText().trim());
                    newStaff.setPosition(positionCombo.getValue());
                    newStaff.setSalary(Double.parseDouble(salaryField.getText().replace(",", "").replace(".", "")));
                    newStaff.setPhoneNumber(phoneField.getText().trim());
                    newStaff.setAddress(addressArea.getText().trim());
                    newStaff.setHireDate(hireDatePicker.getValue());
                    return newStaff;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid salary amount (numbers only).", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });
        
        Optional<Staff> result = dialog.showAndWait();
        result.ifPresent(staff -> {
            if (staffService.addStaff(staff)) {
                showAlert("Success", "Staff member '" + staff.getFullName() + "' added successfully!", Alert.AlertType.INFORMATION);
                loadStaff();
            } else {
                showAlert("Error", "Failed to add staff member. Please try again.", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void showEditStaffDialog(Staff staff) {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Edit Staff");
        dialog.setHeaderText("Edit staff information: " + staff.getFullName());
        
        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");
        
        TextField nameField = new TextField(staff.getFullName());
        nameField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        ComboBox<String> positionCombo = new ComboBox<>();
        positionCombo.getItems().addAll(
            "Security Manager", "Cleaning Supervisor", "Ticketing Staff", 
            "Medical Officer", "Maintenance Head", "Event Coordinator",
            "Cashier", "Customer Service", "IT Support", "Other"
        );
        positionCombo.setValue(staff.getPosition());
        positionCombo.setEditable(true);
        positionCombo.setStyle("-fx-font-size: 13px;");
        
        TextField salaryField = new TextField(String.valueOf((long)staff.getSalary()));
        salaryField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        TextField phoneField = new TextField(staff.getPhoneNumber());
        phoneField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        TextArea addressArea = new TextArea(staff.getAddress());
        addressArea.setPrefRowCount(3);
        addressArea.setWrapText(true);
        addressArea.setStyle("-fx-font-size: 13px;");
        
        grid.add(createLabel("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Position:"), 0, 1);
        grid.add(positionCombo, 1, 1);
        grid.add(createLabel("Monthly Salary (Rp):"), 0, 2);
        grid.add(salaryField, 1, 2);
        grid.add(createLabel("Phone Number:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(createLabel("Address:"), 0, 4);
        grid.add(addressArea, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    staff.setFullName(nameField.getText().trim());
                    staff.setPosition(positionCombo.getValue());
                    staff.setSalary(Double.parseDouble(salaryField.getText().replace(",", "").replace(".", "")));
                    staff.setPhoneNumber(phoneField.getText().trim());
                    staff.setAddress(addressArea.getText().trim());
                    return staff;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid salary amount.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });
        
        Optional<Staff> result = dialog.showAndWait();
        result.ifPresent(updatedStaff -> {
            if (staffService.updateStaff(updatedStaff)) {
                showAlert("Success", "Staff information updated successfully!", Alert.AlertType.INFORMATION);
                loadStaff();
            } else {
                showAlert("Error", "Failed to update staff information.", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void handleToggleStaffStatus(Staff staff) {
        String action = staff.isActive() ? "deactivate" : "activate";
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm " + action.substring(0, 1).toUpperCase() + action.substring(1));
        alert.setHeaderText(action.substring(0, 1).toUpperCase() + action.substring(1) + " staff: " + staff.getFullName());
        alert.setContentText("Are you sure you want to " + action + " this staff member?\n" +
                           (staff.isActive() ? "They will no longer appear in active staff lists." : 
                                              "They will be marked as active again."));
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success;
            if (staff.isActive()) {
                success = staffService.deactivateStaff(staff.getStaffId());
            } else {
                success = staffService.activateStaff(staff.getStaffId());
            }
            
            if (success) {
                showAlert("Success", "Staff member " + action + "d successfully!", Alert.AlertType.INFORMATION);
                loadStaff();
            } else {
                showAlert("Error", "Failed to " + action + " staff member.", Alert.AlertType.ERROR);
            }
        }
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        label.setStyle("-fx-text-fill: #34495e;");
        return label;
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // ==================== CARD-BASED HELPER METHODS ====================
    
    private void refreshCardsDisplay() {
        if (cardsScrollPane != null) {
            FlowPane cardsGrid = new FlowPane();
            cardsGrid.setHgap(20);
            cardsGrid.setVgap(20);
            cardsGrid.setPadding(new Insets(20));
            cardsGrid.setStyle("-fx-background-color: transparent;");
            
            for (Staff staff : staffList) {
                StaffCard card = new StaffCard(
                    staff,
                    () -> showViewDialog(staff),
                    () -> showEditDialog(staff),
                    () -> toggleStaffStatus(staff)
                );
                cardsGrid.getChildren().add(card);
            }
            
            cardsScrollPane.setContent(cardsGrid);
        }
    }
    
    private void showViewDialog(Staff staff) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Staff Details");
        alert.setHeaderText(staff.getFullName() + " - " + staff.getPosition());
        
        String details = String.format(
            "Staff ID: %d\n" +
            "Position: %s\n" +
            "Salary: %s\n" +
            "Phone: %s\n" +
            "Hire Date: %s\n" +
            "Address: %s\n" +
            "Status: %s",
            staff.getStaffId(),
            staff.getPosition(),
            currencyFormat.format(staff.getSalary()),
            staff.getPhoneNumber(),
            staff.getHireDate().format(dateFormatter),
            staff.getAddress(),
            staff.isActive() ? "Active" : "Inactive"
        );
        
        alert.setContentText(details);
        alert.showAndWait();
    }
    
    private void showEditDialog(Staff staff) {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Edit Staff");
        dialog.setHeaderText("Edit: " + staff.getFullName());
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField(staff.getFullName());
        TextField positionField = new TextField(staff.getPosition());
        TextField salaryField = new TextField(String.valueOf(staff.getSalary()));
        TextField phoneField = new TextField(staff.getPhoneNumber());
        TextField addressField = new TextField(staff.getAddress());
        DatePicker hireDatePicker = new DatePicker(staff.getHireDate());
        
        grid.add(createLabel("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Position:"), 0, 1);
        grid.add(positionField, 1, 1);
        grid.add(createLabel("Salary:"), 0, 2);
        grid.add(salaryField, 1, 2);
        grid.add(createLabel("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(createLabel("Hire Date:"), 0, 4);
        grid.add(hireDatePicker, 1, 4);
        grid.add(createLabel("Address:"), 0, 5);
        grid.add(addressField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                staff.setFullName(nameField.getText());
                staff.setPosition(positionField.getText());
                staff.setSalary(Double.parseDouble(salaryField.getText()));
                staff.setPhoneNumber(phoneField.getText());
                staff.setHireDate(hireDatePicker.getValue());
                staff.setAddress(addressField.getText());
                return staff;
            }
            return null;
        });
        
        Optional<Staff> result = dialog.showAndWait();
        result.ifPresent(updatedStaff -> {
            if (staffService.updateStaff(updatedStaff)) {
                showSuccess("Staff updated successfully!");
                loadStaff();
            } else {
                showError("Failed to update staff.");
            }
        });
    }
    
    private void toggleStaffStatus(Staff staff) {
        String action = staff.isActive() ? "deactivate" : "activate";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Status Change");
        alert.setHeaderText(action.substring(0, 1).toUpperCase() + action.substring(1) + " Staff Member");
        alert.setContentText("Are you sure you want to " + action + " " + staff.getFullName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success;
            if (staff.isActive()) {
                success = staffService.deactivateStaff(staff.getStaffId());
            } else {
                success = staffService.activateStaff(staff.getStaffId());
            }
            
            if (success) {
                showSuccess("Staff member " + action + "d successfully!");
                loadStaff();
            } else {
                showError("Failed to " + action + " staff member.");
            }
        }
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
