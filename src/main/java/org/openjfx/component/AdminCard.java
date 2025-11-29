package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.openjfx.model.Admin;

/**
 * Modern card-based UI component for displaying Admin users
 * Features: Gradient backgrounds, role badges, hover animations
 */
public class AdminCard extends VBox {
    
    private final Admin admin;
    private final Runnable onEdit;
    private final Runnable onDelete;
    private final boolean isCurrentUser;
    
    // Color schemes based on role
    private static final String SUPER_ADMIN_GRADIENT = "linear-gradient(135deg, #667eea 0%, #8a16ffff 100%)";
    private static final String ADMIN_GRADIENT = "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)";
    
    public AdminCard(Admin admin, Runnable onEdit, Runnable onDelete, boolean isCurrentUser) {
        this.admin = admin;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.isCurrentUser = isCurrentUser;
        
        initializeUI();
        setupAnimations();
    }
    
    private void initializeUI() {
        this.setPadding(new Insets(0));
        this.setSpacing(0);
        this.setAlignment(Pos.TOP_LEFT);
        
        // FIXED SIZE - Tetap kotak tidak berubah (increased to 340x500)
        this.setPrefWidth(340);
        this.setMinWidth(340);
        this.setMaxWidth(340);
        this.setPrefHeight(500);
        this.setMinHeight(500);
        this.setMaxHeight(500);
        
        // Base card styling with shadow layers
        this.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(122, 116, 116, 0.1), 15, 0.0, 0, 5);" +
            "-fx-cursor: hand;"
        );
        
        // Top colored header with gradient
        VBox header = createHeader();
        
        // Content section
        VBox content = createContent();
        
        // Action buttons section
        HBox actions = createActionButtons();
        
        this.getChildren().addAll(header, content, actions);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(12);
        header.setPadding(new Insets(25, 25, 20, 25));
        header.setAlignment(Pos.TOP_LEFT);
        
        // Set gradient based on role
        String gradient = admin.isSuperAdmin() ? SUPER_ADMIN_GRADIENT : ADMIN_GRADIENT;
        header.setStyle(
            "-fx-background-color: " + gradient + ";" +
            "-fx-background-radius: 20 20 0 0;"
        );
        
        // Top row: Icon and role badge
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Icon with circular background
        HBox iconBox = new HBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefSize(50, 50);
        iconBox.setMaxSize(50, 50);
        iconBox.setStyle(
            "-fx-background-color: rgba(119, 228, 255, 0.57);" +
            "-fx-background-radius: 25;" +
            "-fx-effect: dropshadow(gaussian, rgba(189, 189, 189, 0.62), 8, 0.0, 0, 2);"
        );
        
        FontAwesomeIcon iconType = admin.isSuperAdmin() ? 
            FontAwesomeIcon.USER_SECRET : FontAwesomeIcon.USER;
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("24");
        icon.setGlyphStyle(
            "-fx-fill: gr;" +
            "-fx-font-family: FontAwesome;" +
            "-fx-font-size: 24px;"
        );
        iconBox.getChildren().add(icon);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Role badge
        Label roleBadge = createRoleBadge();
        
        topRow.getChildren().addAll(iconBox, spacer, roleBadge);
        
        // Admin username
        Label usernameLabel = new Label(admin.getUsername());
        usernameLabel.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: rgba(31, 31, 31, 0.68);" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-effect: dropshadow(gaussian, rgba(177, 177, 177, 1), 2, 0.0, 0, 1);"
        );
        usernameLabel.setWrapText(true);
        usernameLabel.setMaxWidth(270);
        
        // ID label
        Label idLabel = new Label("ID: #" + admin.getId());
        idLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: rgba(255,255,255,0.9);" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );
        
        // Current user indicator
        if (isCurrentUser) {
            HBox currentBadge = new HBox(6);
            currentBadge.setAlignment(Pos.CENTER_LEFT);
            currentBadge.setPadding(new Insets(4, 0, 0, 0));
            
         
            Label currentLabel = new Label("You");
            currentLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 500;" +
                "-fx-text-fill: rgba(255,255,255,0.95);" +
                "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
            );
            
            header.getChildren().addAll(topRow, usernameLabel, idLabel, currentBadge);
        } else {
            header.getChildren().addAll(topRow, usernameLabel, idLabel);
        }
        
        return header;
    }
    
    private Label createRoleBadge() {
        String text = admin.isSuperAdmin() ? "SUPER" : "ADMIN";
        String badgeColor = admin.isSuperAdmin() ? 
            "rgba(235, 0, 0, 0.95)" : "rgba(80, 235, 145, 0.95)";
        
        Label badge = new Label(text);
        badge.setPadding(new Insets(6, 14, 6, 14));
        badge.setStyle(
            "-fx-background-color: " + badgeColor + ";" +
            "-fx-background-radius: 20;" +
            "-fx-text-fill: rgba(0, 0, 0, 0.95);" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 700;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.88), 4, 0.0, 0, 2);"
        );
        
        return badge;
    }
    
    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        
        // Username info
        VBox usernameBox = createInfoRow(
            FontAwesomeIcon.USER,
            "Username",
            admin.getUsername(),
            "#6366f1"
        );
        
        // ID info
        VBox idBox = createInfoRow(
            FontAwesomeIcon.HASHTAG,
            "Admin ID",
            "#" + admin.getId(),
            "#10b981"
        );
        
        // Role info
        VBox roleBox = createInfoRow(
            FontAwesomeIcon.SHIELD,
            "Role",
            admin.getRole(),
            "#f59e0b"
        );
        
        // Status info
        VBox statusBox = createInfoRow(
            FontAwesomeIcon.CHECK_CIRCLE,
            "Status",
            "Active",
            "#8b5cf6"
        );
        
        content.getChildren().addAll(usernameBox, idBox, roleBox, statusBox);
        return content;
    }
    
    private VBox createInfoRow(FontAwesomeIcon iconType, String label, String value, String color) {
        VBox row = new VBox(6);
        
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        // Icon
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("14");
        icon.setGlyphStyle(
            "-fx-fill: " + color + ";" +
            "-fx-font-family: FontAwesome;" +
            "-fx-font-size: 14px;"
        );
        
        // Label
        Label labelText = new Label(label);
        labelText.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #6b7280;" +
            "-fx-font-weight: 600;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );
        
        headerRow.getChildren().addAll(icon, labelText);
        
        // Value
        Label valueText = new Label(value);
        valueText.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-text-fill: #646568ff;" +
            "-fx-font-weight: 600;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );
        valueText.setWrapText(true);
        
        row.getChildren().addAll(headerRow, valueText);
        return row;
    }
    
    private HBox createActionButtons() {
        HBox actions = new HBox(12);
        actions.setPadding(new Insets(20, 25, 25, 25));
        actions.setAlignment(Pos.CENTER);
        
        // Edit button
        Button editBtn = createActionButton(
            "Edit",
            FontAwesomeIcon.EDIT,
            "#3b82f6",
            "#2563eb",
            onEdit
        );
        
        // Delete button (disabled for current user)
        Button deleteBtn = createActionButton(
            "Delete",
            FontAwesomeIcon.TRASH,
            isCurrentUser ? "#9ca3af" : "#ef4444",
            isCurrentUser ? "#6b7280" : "#dc2626",
            isCurrentUser ? null : onDelete
        );
        
        if (isCurrentUser) {
            deleteBtn.setDisable(true);
            deleteBtn.setOpacity(0.5);
        }
        
        HBox.setHgrow(editBtn, Priority.ALWAYS);
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);
        editBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        return actions;
    }
    
    private Button createActionButton(String text, FontAwesomeIcon iconType, 
                                     String baseColor, String hoverColor, Runnable action) {
        Button btn = new Button(text);
        btn.setPadding(new Insets(12, 20, 12, 20));
        btn.setStyle(
            "-fx-background-color: " + baseColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-effect: dropshadow(gaussian, " + baseColor + "40, 6, 0.0, 0, 2);"
        );
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("14");
        icon.setGlyphStyle(
            "-fx-fill: white;" +
            "-fx-font-family: FontAwesome;" +
            "-fx-font-size: 14px;"
        );
        btn.setGraphic(icon);
        
        if (action != null) {
            btn.setOnMouseEntered(e -> {
                btn.setStyle(
                    "-fx-background-color: " + hoverColor + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 10;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
                    "-fx-effect: dropshadow(gaussian, " + hoverColor + "60, 8, 0.0, 0, 3);"
                );
            });
            
            btn.setOnMouseExited(e -> {
                btn.setStyle(
                    "-fx-background-color: " + baseColor + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 10;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
                    "-fx-effect: dropshadow(gaussian, " + baseColor + "40, 6, 0.0, 0, 2);"
                );
            });
            
            btn.setOnAction(e -> action.run());
        }
        
        return btn;
    }
    
    private void setupAnimations() {
        // Hover scale animation
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
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0.0, 0, 10);" +
                "-fx-cursor: hand;"
            );
        });
        
        this.setOnMouseExited(e -> {
            scaleOut.playFromStart();
            this.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.0, 0, 5);" +
                "-fx-cursor: hand;"
            );
        });
        
        // Entry animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), this);
        slideIn.setFromY(30);
        slideIn.setToY(0);
        
        ParallelTransition entry = new ParallelTransition(fadeIn, slideIn);
        entry.play();
    }
    
    public Admin getAdmin() {
        return admin;
    }
}
