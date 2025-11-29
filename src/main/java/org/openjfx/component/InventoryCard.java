package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.openjfx.model.InventoryItem;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Modern card component for Inventory display - Fixed size kotak
 */
public class InventoryCard extends VBox {
    
    private final InventoryItem item;
    private final Runnable onEdit;
    private final Runnable onDelete;
    private final NumberFormat currencyFormat;
    
    private static final String HIGH_STOCK_GRADIENT = "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
    private static final String MEDIUM_STOCK_GRADIENT = "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)";
    private static final String LOW_STOCK_GRADIENT = "linear-gradient(135deg, #fa709a 0%, #fee140 100%)";
    private static final String OUT_OF_STOCK_GRADIENT = "linear-gradient(135deg, #a8caba 0%, #5d4157 100%)";
    
    public InventoryCard(InventoryItem item, Runnable onEdit, Runnable onDelete) {
        this.item = item;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        initializeUI();
        setupAnimations();
    }
    
    private void initializeUI() {
        this.setPadding(new Insets(0));
        this.setSpacing(0);
        
        // FIXED SIZE - Tetap kotak tidak berubah (increased to 340x500)
        this.setPrefWidth(340);
        this.setMinWidth(340);
        this.setMaxWidth(340);
        this.setPrefHeight(500);
        this.setMinHeight(500);
        this.setMaxHeight(500);
        
        this.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);"
        );
        
        this.getChildren().addAll(createHeader(), createContent(), createActions());
    }
    
    private VBox createHeader() {
        VBox header = new VBox(12);
        header.setPadding(new Insets(25));
        String gradient = getStockGradient();
        header.setStyle("-fx-background-color: " + gradient + "; -fx-background-radius: 20 20 0 0;");
        
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        HBox iconBox = new HBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefSize(50, 50);
        iconBox.setStyle(
            "-fx-background-color: rgba(82, 82, 82, 0.75);" +
            "-fx-background-radius: 25;"
        );
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.CUBE);
        icon.setSize("24");
        icon.setGlyphStyle("-fx-fill: rgba(0, 0, 0, 0.75); -fx-font-family: FontAwesome; -fx-font-size: 24px;");
        iconBox.getChildren().add(icon);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label stockBadge = createStockBadge();
        topRow.getChildren().addAll(iconBox, spacer, stockBadge);
        
        Label nameLabel = new Label(item.getItemName());
        nameLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: rgba(80, 80, 80, 1);" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 2, 0, 0, 1);"
        );  
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(270);
        nameLabel.setMaxHeight(50);
        
        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: rgba(0, 0, 0, 0.9);"
        );
        
        header.getChildren().addAll(topRow, nameLabel, categoryLabel);
        return header;
    }
    
    private Label createStockBadge() {
        int quantity = item.getQuantity();
        String text;
        String badgeColor;
        
        if (quantity == 0) {
            text = "OUT";
            badgeColor = "rgba(239, 68, 68, 0.95)";
        } else if (quantity < item.getMinStockLevel()) {
            text = "LOW";
            badgeColor = "rgba(251, 191, 36, 0.95)";
        } else if (quantity < 50) {
            text = "OK";
            badgeColor = "rgba(59, 130, 246, 0.95)";
        } else {
            text = "HIGH";
            badgeColor = "rgba(34, 197, 94, 0.95)";
        }
        
        Label badge = new Label(text);
        badge.setPadding(new Insets(6, 14, 6, 14));
        badge.setStyle(
            "-fx-background-color: " + badgeColor + ";" +
            "-fx-background-radius: 20;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 700;"
        );
        
        return badge;
    }
    
    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        
        content.getChildren().addAll(
            createInfoRow(FontAwesomeIcon.CUBES, "Quantity", item.getQuantity() + " units", "#6366f1"),
            createInfoRow(FontAwesomeIcon.MONEY, "Unit Price", currencyFormat.format(item.getUnitPrice()), "#10b981"),
            createInfoRow(FontAwesomeIcon.MAP_MARKER, "Location", item.getLocation(), "#f59e0b"),
            createInfoRow(FontAwesomeIcon.EXCLAMATION_TRIANGLE, "Min Stock", item.getMinStockLevel() + " units", "#ef4444")
        );
        
        return content;
    }
    
    private VBox createInfoRow(FontAwesomeIcon iconType, String label, String value, String color) {
        VBox row = new VBox(6);
        
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("14");
        icon.setGlyphStyle("-fx-fill: " + color + "; -fx-font-family: FontAwesome; -fx-font-size: 14px;");
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
        
        headerRow.getChildren().addAll(icon, labelText);
        
        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 15px; -fx-text-fill: #111827; -fx-font-weight: 600;");
        valueText.setWrapText(true);
        valueText.setMaxWidth(260);
        
        row.getChildren().addAll(headerRow, valueText);
        return row;
    }
    
    private HBox createActions() {
        HBox actions = new HBox(12);
        actions.setPadding(new Insets(20, 25, 25, 25));
        actions.setAlignment(Pos.CENTER);
        
        Button editBtn = createButton("Edit", FontAwesomeIcon.EDIT, "#3b82f6", onEdit);
        Button deleteBtn = createButton("Delete", FontAwesomeIcon.TRASH, "#ef4444", onDelete);
        
        HBox.setHgrow(editBtn, Priority.ALWAYS);
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);
        editBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        return actions;
    }
    
    private Button createButton(String text, FontAwesomeIcon iconType, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setPadding(new Insets(12, 20, 12, 20));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("14");
        icon.setGlyphStyle("-fx-fill: white; -fx-font-family: FontAwesome; -fx-font-size: 14px;");
        btn.setGraphic(icon);
        
        if (action != null) {
            btn.setOnAction(e -> action.run());
        }
        
        return btn;
    }
    
    private String getStockGradient() {
        int quantity = item.getQuantity();
        
        if (quantity == 0) {
            return OUT_OF_STOCK_GRADIENT;
        } else if (quantity < item.getMinStockLevel()) {
            return LOW_STOCK_GRADIENT;
        } else if (quantity < 50) {
            return MEDIUM_STOCK_GRADIENT;
        } else {
            return HIGH_STOCK_GRADIENT;
        }
    }
    
    private void setupAnimations() {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), this);
        scaleIn.setToX(1.03);
        scaleIn.setToY(1.03);
        
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), this);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        
        this.setOnMouseEntered(e -> {
            scaleIn.playFromStart();
            this.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0, 0, 10);"
            );
        });
        
        this.setOnMouseExited(e -> {
            scaleOut.playFromStart();
            this.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);"
            );
        });
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), this);
        slideIn.setFromY(30);
        slideIn.setToY(0);
        
        new ParallelTransition(fadeIn, slideIn).play();
    }
    
    public InventoryItem getItem() {
        return item;
    }
}
