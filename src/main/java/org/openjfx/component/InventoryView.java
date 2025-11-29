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
import org.openjfx.model.InventoryItem;
import org.openjfx.service.InventoryService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Complete Inventory Management View with TableView
 * Manages stadium inventory items with low stock alerts
 */
public class InventoryView extends VBox {
    
    private final InventoryService inventoryService;
    private ObservableList<InventoryItem> inventoryList;
    private final NumberFormat currencyFormat;
    
    private Label totalItemsLabel;
    private Label lowStockLabel;
    private Label totalValueLabel;
    private CheckBox showLowStockOnly;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> stockLevelFilter;
    private ScrollPane cardsScrollPane; // Store reference to update cards
    
    public InventoryView() {
        this.inventoryService = new InventoryService();
        this.inventoryList = FXCollections.observableArrayList();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        setupUI();
        loadInventory();
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
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.CUBES);
        icon.setSize("32");
        icon.setGlyphStyle("-fx-fill: #2c3e50;");
        
        Label titleLabel = new Label("Inventory Management");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        titleBox.getChildren().addAll(icon, titleLabel);
        
        Label descLabel = new Label("Track stadium inventory, stock levels, and automatic low stock alerts");
        descLabel.setFont(Font.font("Arial", 13));
        descLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        header.getChildren().addAll(titleBox, descLabel);
        return header;
    }
    
    private HBox createStatsCards() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Total Items Card
        VBox totalCard = createStatCard("Total Items", "0", "#3498db", FontAwesomeIcon.ARCHIVE);
        totalItemsLabel = (Label) totalCard.lookup("#stat-value");
        
        // Low Stock Card
        VBox lowStockCard = createStatCard("Low Stock Items", "0", "#e74c3c", FontAwesomeIcon.EXCLAMATION_TRIANGLE);
        lowStockLabel = (Label) lowStockCard.lookup("#stat-value");
        
        // Total Value Card
        VBox valueCard = createStatCard("Total Inventory Value", "Rp 0", "#27ae60", FontAwesomeIcon.DOLLAR);
        totalValueLabel = (Label) valueCard.lookup("#stat-value");
        
        statsBox.getChildren().addAll(totalCard, lowStockCard, valueCard);
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
        
        Button addButton = new Button("Add New Item");
        FontAwesomeIconView addIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        addIcon.setSize("14");
        addIcon.setGlyphStyle("-fx-fill: white;");
        addButton.setGraphic(addIcon);
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        addButton.setOnAction(e -> showAddItemDialog());
        
        Button refreshButton = new Button("Refresh");
        FontAwesomeIconView refreshIcon = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        refreshIcon.setSize("14");
        refreshIcon.setGlyphStyle("-fx-fill: white;");
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                              "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> loadInventory());
        
        // Stock Level Filter
        Label stockFilterLabel = new Label("Stock:");
        stockFilterLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        stockFilterLabel.setStyle("-fx-text-fill: #34495e;");
        
        stockLevelFilter = new ComboBox<>();
        stockLevelFilter.getItems().addAll("All Items", "High Stock", "Medium Stock", "Low Stock", "Out of Stock");
        stockLevelFilter.setValue("All Items");
        stockLevelFilter.setStyle("-fx-font-size: 12px; -fx-background-radius: 5;");
        stockLevelFilter.setOnAction(e -> filterInventory());
        
        // Category Filter
        Label categoryLabel = new Label("Category:");
        categoryLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        categoryLabel.setStyle("-fx-text-fill: #34495e;");
        
        categoryFilter = new ComboBox<>();
        categoryFilter.setValue("All Categories");
        categoryFilter.setStyle("-fx-font-size: 12px; -fx-background-radius: 5;");
        categoryFilter.setOnAction(e -> filterInventory());
        
        // Low Stock Checkbox
        showLowStockOnly = new CheckBox("Low Stock Alert");
        showLowStockOnly.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        showLowStockOnly.setStyle("-fx-text-fill: #e74c3c;");
        showLowStockOnly.setSelected(false);
        showLowStockOnly.setOnAction(e -> filterInventory());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label countLabel = new Label();
        countLabel.setId("count-label");
        countLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        countLabel.setStyle("-fx-text-fill: #34495e; -fx-background-color: #ecf0f1; " +
                           "-fx-padding: 8 15; -fx-background-radius: 5;");
        
        toolbar.getChildren().addAll(
            addButton, refreshButton, 
            new Separator(javafx.geometry.Orientation.VERTICAL),
            stockFilterLabel, stockLevelFilter,
            categoryLabel, categoryFilter,
            showLowStockOnly,
            spacer, countLabel
        );
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
        
        for (InventoryItem item : inventoryList) {
            InventoryCard card = new InventoryCard(
                item,
                () -> showEditDialog(item),
                () -> showDeleteDialog(item)
            );
            cardsGrid.getChildren().add(card);
        }
        
        scrollPane.setContent(cardsGrid);
        return scrollPane;
    }
    
    private TableView<InventoryItem> createInventoryTable() {
        TableView<InventoryItem> table = new TableView<>();
        table.setItems(inventoryList);
        table.setStyle("-fx-background-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // ID Column
        TableColumn<InventoryItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");
        
        // Item Name Column
        TableColumn<InventoryItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        nameCol.setPrefWidth(200);
        nameCol.setCellFactory(col -> new TableCell<InventoryItem, String>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    FontAwesomeIconView itemIcon = new FontAwesomeIconView(FontAwesomeIcon.CUBE);
                    itemIcon.setSize("16");
                    itemIcon.setGlyphStyle("-fx-fill: #3498db;");
                    
                    Label nameLabel = new Label(name);
                    nameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
                    
                    box.getChildren().addAll(itemIcon, nameLabel);
                    setGraphic(box);
                }
            }
        });
        
        // Category Column
        TableColumn<InventoryItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);
        categoryCol.setCellFactory(col -> new TableCell<InventoryItem, String>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(category);
                    badge.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 11));
                    badge.setPadding(new Insets(4, 10, 4, 10));
                    badge.setStyle("-fx-background-color: #e8f4f8; -fx-text-fill: #2980b9; -fx-background-radius: 10;");
                    setGraphic(badge);
                }
            }
        });
        
        // Quantity Column
        TableColumn<InventoryItem, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        quantityCol.setCellFactory(col -> new TableCell<InventoryItem, Integer>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    setText(String.valueOf(quantity));
                    setFont(Font.font("Arial", FontWeight.BOLD, 13));
                    
                    if (item.isLowStock()) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-alignment: CENTER;");
                    }
                }
            }
        });
        
        // Min Stock Column
        TableColumn<InventoryItem, Integer> minStockCol = new TableColumn<>("Min Stock");
        minStockCol.setCellValueFactory(new PropertyValueFactory<>("minimumStock"));
        minStockCol.setPrefWidth(90);
        minStockCol.setStyle("-fx-alignment: CENTER;");
        
        // Unit Price Column
        TableColumn<InventoryItem, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(col -> new TableCell<InventoryItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(price));
                    setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
                    setStyle("-fx-text-fill: #27ae60;");
                }
            }
        });
        
        // Status Column
        TableColumn<InventoryItem, Boolean> statusCol = new TableColumn<>("Stock Status");
        statusCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isLowStock())
        );
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<InventoryItem, Boolean>() {
            @Override
            protected void updateItem(Boolean isLowStock, boolean empty) {
                super.updateItem(isLowStock, empty);
                if (empty || isLowStock == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusBadge = new Label(isLowStock ? "⚠ LOW STOCK" : "✓ SUFFICIENT");
                    statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                    statusBadge.setPadding(new Insets(5, 12, 5, 12));
                    statusBadge.setStyle(isLowStock ? 
                        "-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; -fx-background-radius: 12;" :
                        "-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-background-radius: 12;");
                    setGraphic(statusBadge);
                }
            }
        });
        
        // Actions Column
        TableColumn<InventoryItem, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(240);
        actionsCol.setCellFactory(col -> new TableCell<InventoryItem, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button addStockButton = new Button("+10");
            private final Button reduceStockButton = new Button("-10");
            private final HBox buttonsBox = new HBox(6, editButton, addStockButton, reduceStockButton);
            
            {
                // Edit Button
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
                editIcon.setSize("12");
                editIcon.setGlyphStyle("-fx-fill: white;");
                editButton.setGraphic(editIcon);
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                                   "-fx-font-size: 10px; -fx-padding: 5 10; -fx-background-radius: 4; -fx-cursor: hand;");
                
                // Add Stock Button
                addStockButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 5 12; " +
                                       "-fx-background-radius: 4; -fx-cursor: hand;");
                
                // Reduce Stock Button
                reduceStockButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                          "-fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 5 12; " +
                                          "-fx-background-radius: 4; -fx-cursor: hand;");
                
                editButton.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    showEditItemDialog(item);
                });
                
                addStockButton.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleQuickStockUpdate(item, 10);
                });
                
                reduceStockButton.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleQuickStockUpdate(item, -10);
                });
                
                buttonsBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, categoryCol, quantityCol, minStockCol, priceCol, statusCol, actionsCol);
        
        // Row factory for highlighting low stock items
        table.setRowFactory(tv -> new TableRow<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isLowStock()) {
                    setStyle("-fx-background-color: #fff5f5;");
                } else {
                    setStyle("");
                }
            }
        });
        
        // Empty state
        table.setPlaceholder(new Label("No inventory items found. Click 'Add New Item' to add one."));
        
        return table;
    }
    
    private void loadInventory() {
        List<InventoryItem> allItems = inventoryService.getAllItems();
        inventoryList.clear();
        inventoryList.addAll(allItems);
        
        // Populate category filter with unique categories
        if (categoryFilter != null) {
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            allItems.stream()
                .map(InventoryItem::getCategory)
                .distinct()
                .sorted()
                .forEach(categoryFilter.getItems()::add);
            if (categoryFilter.getValue() == null) {
                categoryFilter.setValue("All Categories");
            }
        }
        
        updateStats();
        updateCountLabel();
        refreshCardsDisplay(); // Rebuild UI cards after data loaded
    }
    
    private void filterInventory() {
        List<InventoryItem> allItems = inventoryService.getAllItems();
        inventoryList.clear();
        
        // Apply filters
        for (InventoryItem item : allItems) {
            boolean matches = true;
            
            // Filter by low stock only
            if (showLowStockOnly != null && showLowStockOnly.isSelected()) {
                if (!item.isLowStock()) {
                    matches = false;
                }
            }
            
            // Filter by category
            if (categoryFilter != null && categoryFilter.getValue() != null) {
                String selectedCategory = categoryFilter.getValue();
                if (!"All Categories".equals(selectedCategory)) {
                    if (!item.getCategory().equals(selectedCategory)) {
                        matches = false;
                    }
                }
            }
            
            // Filter by stock level
            if (stockLevelFilter != null && stockLevelFilter.getValue() != null) {
                String selectedLevel = stockLevelFilter.getValue();
                if (!"All Items".equals(selectedLevel)) {
                    int quantity = item.getQuantity();
                    int minStock = item.getMinStockLevel();
                    
                    switch (selectedLevel) {
                        case "High Stock":
                            if (quantity < 50) matches = false;
                            break;
                        case "Medium Stock":
                            if (quantity >= 50 || quantity < minStock) matches = false;
                            break;
                        case "Low Stock":
                            if (quantity == 0 || quantity >= minStock) matches = false;
                            break;
                        case "Out of Stock":
                            if (quantity != 0) matches = false;
                            break;
                    }
                }
            }
            
            if (matches) {
                inventoryList.add(item);
            }
        }
        
        updateStats();
        updateCountLabel();
        refreshCardsDisplay();
    }
    
    private void updateStats() {
        int totalItems = inventoryList.size();
        long lowStockCount = inventoryList.stream().filter(InventoryItem::isLowStock).count();
        double totalValue = inventoryList.stream()
            .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
            .sum();
        
        if (totalItemsLabel != null) totalItemsLabel.setText(String.valueOf(totalItems));
        if (lowStockLabel != null) {
            lowStockLabel.setText(String.valueOf(lowStockCount));
            if (lowStockCount > 0) {
                lowStockLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        }
        if (totalValueLabel != null) {
            totalValueLabel.setText(currencyFormat.format(totalValue));
            totalValueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        }
    }
    
    private void updateCountLabel() {
        Label countLabel = (Label) lookup("#count-label");
        if (countLabel != null) {
            long lowStockCount = inventoryList.stream().filter(InventoryItem::isLowStock).count();
            countLabel.setText("Showing " + inventoryList.size() + " items (" + lowStockCount + " low stock)");
        }
    }
    
    private void showAddItemDialog() {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Item");
        dialog.setHeaderText("Add a new item to inventory");
        
        ButtonType addButtonType = new ButtonType("Add Item", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Football Ball");
        nameField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "Sports Equipment", "Food & Beverage", "Merchandise", 
            "Cleaning Supplies", "Safety Equipment", "Office Supplies",
            "Medical Supplies", "Technical Equipment", "Other"
        );
        categoryCombo.setPromptText("Select category");
        categoryCombo.setEditable(true);
        categoryCombo.setStyle("-fx-font-size: 13px;");
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("e.g., 50");
        quantityField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        TextField minStockField = new TextField();
        minStockField.setPromptText("e.g., 10");
        minStockField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        TextField priceField = new TextField();
        priceField.setPromptText("e.g., 150000");
        priceField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        grid.add(createLabel("Item Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Category:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(createLabel("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(createLabel("Minimum Stock:"), 0, 3);
        grid.add(minStockField, 1, 3);
        grid.add(createLabel("Unit Price (Rp):"), 0, 4);
        grid.add(priceField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/disable add button
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        nameField.textProperty().addListener((obs, old, newVal) -> {
            addButton.setDisable(newVal.trim().isEmpty() || quantityField.getText().isEmpty());
        });
        quantityField.textProperty().addListener((obs, old, newVal) -> {
            addButton.setDisable(newVal.trim().isEmpty() || nameField.getText().trim().isEmpty());
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    InventoryItem newItem = new InventoryItem();
                    newItem.setItemName(nameField.getText().trim());
                    newItem.setCategory(categoryCombo.getValue());
                    newItem.setQuantity(Integer.parseInt(quantityField.getText()));
                    newItem.setMinimumStock(Integer.parseInt(minStockField.getText()));
                    newItem.setUnitPrice(Double.parseDouble(priceField.getText().replace(",", "").replace(".", "")));
                    return newItem;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers for quantity, minimum stock, and price.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });
        
        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            if (inventoryService.addItem(item)) {
                showAlert("Success", "Item '" + item.getItemName() + "' added successfully!", Alert.AlertType.INFORMATION);
                loadInventory();
            } else {
                showAlert("Error", "Failed to add item. Please try again.", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void showEditItemDialog(InventoryItem item) {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");
        dialog.setHeaderText("Edit inventory item: " + item.getItemName());
        
        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");
        
        TextField nameField = new TextField(item.getItemName());
        nameField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "Sports Equipment", "Food & Beverage", "Merchandise", 
            "Cleaning Supplies", "Safety Equipment", "Office Supplies",
            "Medical Supplies", "Technical Equipment", "Other"
        );
        categoryCombo.setValue(item.getCategory());
        categoryCombo.setEditable(true);
        categoryCombo.setStyle("-fx-font-size: 13px;");
        
        TextField quantityField = new TextField(String.valueOf(item.getQuantity()));
        quantityField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        TextField minStockField = new TextField(String.valueOf(item.getMinimumStock()));
        minStockField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        TextField priceField = new TextField(String.valueOf((long)item.getUnitPrice()));
        priceField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        
        grid.add(createLabel("Item Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Category:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(createLabel("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(createLabel("Minimum Stock:"), 0, 3);
        grid.add(minStockField, 1, 3);
        grid.add(createLabel("Unit Price (Rp):"), 0, 4);
        grid.add(priceField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    item.setItemName(nameField.getText().trim());
                    item.setCategory(categoryCombo.getValue());
                    item.setQuantity(Integer.parseInt(quantityField.getText()));
                    item.setMinimumStock(Integer.parseInt(minStockField.getText()));
                    item.setUnitPrice(Double.parseDouble(priceField.getText().replace(",", "").replace(".", "")));
                    return item;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });
        
        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(updatedItem -> {
            if (inventoryService.updateItem(updatedItem)) {
                showAlert("Success", "Item updated successfully!", Alert.AlertType.INFORMATION);
                loadInventory();
            } else {
                showAlert("Error", "Failed to update item.", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void handleQuickStockUpdate(InventoryItem item, int change) {
        int newQuantity = item.getQuantity() + change;
        if (newQuantity < 0) {
            showAlert("Invalid Operation", "Cannot reduce stock below 0.", Alert.AlertType.WARNING);
            return;
        }
        
        item.setQuantity(newQuantity);
        if (inventoryService.updateItem(item)) {
            loadInventory();
            showAlert("Success", "Stock updated: " + item.getItemName() + " now has " + newQuantity + " units", 
                     Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "Failed to update stock.", Alert.AlertType.ERROR);
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
            
            for (InventoryItem item : inventoryList) {
                InventoryCard card = new InventoryCard(
                    item,
                    () -> showEditDialog(item),
                    () -> showDeleteDialog(item)
                );
                cardsGrid.getChildren().add(card);
            }
            
            cardsScrollPane.setContent(cardsGrid);
        }
    }
    
    private void showEditDialog(InventoryItem item) {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");
        dialog.setHeaderText("Edit: " + item.getItemName());
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField(item.getItemName());
        TextField categoryField = new TextField(item.getCategory());
        TextField quantityField = new TextField(String.valueOf(item.getQuantity()));
        TextField minStockField = new TextField(String.valueOf(item.getMinStockLevel()));
        TextField priceField = new TextField(String.valueOf(item.getUnitPrice()));
        TextField locationField = new TextField(item.getLocation());
        
        grid.add(createLabel("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(createLabel("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(createLabel("Min Stock:"), 0, 3);
        grid.add(minStockField, 1, 3);
        grid.add(createLabel("Price:"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(createLabel("Location:"), 0, 5);
        grid.add(locationField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                item.setItemName(nameField.getText());
                item.setCategory(categoryField.getText());
                item.setQuantity(Integer.parseInt(quantityField.getText()));
                item.setMinStockLevel(Integer.parseInt(minStockField.getText()));
                item.setUnitPrice(Double.parseDouble(priceField.getText()));
                item.setLocation(locationField.getText());
                return item;
            }
            return null;
        });
        
        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(updatedItem -> {
            if (inventoryService.updateItem(updatedItem)) {
                showSuccess("Item updated successfully!");
                loadInventory();
            } else {
                showError("Failed to update item.");
            }
        });
    }
    
    private void showDeleteDialog(InventoryItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Item: " + item.getItemName());
        alert.setContentText("Are you sure you want to delete this item? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (inventoryService.deleteItem(item.getItemId())) {
                showSuccess("Item deleted successfully!");
                loadInventory();
            } else {
                showError("Failed to delete item.");
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
