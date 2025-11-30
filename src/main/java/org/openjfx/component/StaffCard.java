package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.openjfx.model.Staff;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Modern card component for Staff display
 */
public class StaffCard extends VBox {
    
    private final Staff staff;
    private final Runnable onView;
    private final Runnable onEdit;
    private final Runnable onToggle;
    private final NumberFormat currencyFormat;
    private final DateTimeFormatter dateFormatter;
    
    private static final String ACTIVE_GRADIENT = "linear-gradient(135deg, #10b981 0%, #059669 100%)";
    private static final String INACTIVE_GRADIENT = "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)";
    
    public StaffCard(Staff staff, Runnable onView, Runnable onEdit, Runnable onToggle) {
        this.staff = staff;
        this.onView = onView;
        this.onEdit = onEdit;
        this.onToggle = onToggle;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        this.dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        
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
        String gradient = staff.isActive() ? ACTIVE_GRADIENT : INACTIVE_GRADIENT;
        header.setStyle("-fx-background-color: " + gradient + "; -fx-background-radius: 20 20 0 0;");
        
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        HBox iconBox = new HBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefSize(50, 50);
        iconBox.setStyle(
            "-fx-background-color: rgba(255,255,255,0.25);" +
            "-fx-background-radius: 25;"
        );
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.USER);
        icon.setSize("24");
        icon.setGlyphStyle("-fx-fill: rgba(63, 178, 255, 0.9); -fx-font-family: FontAwesome; -fx-font-size: 24px;");
        iconBox.getChildren().add(icon);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusBadge = new Label(staff.isActive() ? "ACTIVE" : "INACTIVE");
        statusBadge.setPadding(new Insets(6, 14, 6, 14));
        String badgeColor = staff.isActive() ? 
            "rgba(150, 150, 150, 0.95)" : 
            "rgba(70, 65, 65, 0.95)";   
        String badgeTextColor = staff.isActive() ?
            "#09ff00ff" : 
            "#dc2626";  
        statusBadge.setStyle(
            "-fx-background-color: " + badgeColor + ";" +
            "-fx-background-radius: 20;" +
            "-fx-text-fill: " + badgeTextColor + ";" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 700;"
        );
        
        topRow.getChildren().addAll(iconBox, spacer, statusBadge);
        
        Label nameLabel = new Label(staff.getFullName());
        nameLabel.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: rgba(35, 156, 248, 0.87);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);"
        );
        nameLabel.setWrapText(true);
        
        Label positionLabel = new Label(staff.getPosition());
        positionLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: rgba(0, 0, 0, 0.9);"
        );
        
        header.getChildren().addAll(topRow, nameLabel, positionLabel);
        return header;
    }
    
    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        
        content.getChildren().addAll(
            createInfoRow(FontAwesomeIcon.MONEY, "Salary", currencyFormat.format(staff.getSalary()), "#10b981"),
            createInfoRow(FontAwesomeIcon.PHONE, "Phone", staff.getPhoneNumber(), "#6366f1"),
            createInfoRow(FontAwesomeIcon.CALENDAR, "Hire Date", staff.getHireDate().format(dateFormatter), "#f59e0b"),
            createInfoRow(FontAwesomeIcon.MAP_MARKER, "Address", staff.getAddress(), "#8b5cf6")
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
        
        row.getChildren().addAll(headerRow, valueText);
        return row;
    }
    
    private HBox createActions() {
        HBox actions = new HBox(12);
        actions.setPadding(new Insets(20, 25, 25, 25));
        actions.setAlignment(Pos.CENTER);
        
        Button viewBtn = createButton("View", FontAwesomeIcon.EYE, "#3b82f6", onView);
        Button editBtn = createButton("Edit", FontAwesomeIcon.EDIT, "#10b981", onEdit);
        Button toggleBtn = createButton(
            staff.isActive() ? "Deactivate" : "Activate", 
            staff.isActive() ? FontAwesomeIcon.BAN : FontAwesomeIcon.CHECK,
            staff.isActive() ? "#ef4444" : "#22c55e",
            onToggle
        );
        
        HBox.setHgrow(viewBtn, Priority.ALWAYS);
        HBox.setHgrow(editBtn, Priority.ALWAYS);
        HBox.setHgrow(toggleBtn, Priority.ALWAYS);
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.setMaxWidth(Double.MAX_VALUE);
        toggleBtn.setMaxWidth(Double.MAX_VALUE);
        
        actions.getChildren().addAll(viewBtn, editBtn, toggleBtn);
        return actions;
    }
    
    private Button createButton(String text, FontAwesomeIcon iconType, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("12");
        icon.setGlyphStyle("-fx-fill: white; -fx-font-family: FontAwesome; -fx-font-size: 12px;");
        btn.setGraphic(icon);
        
        if (action != null) {
            btn.setOnAction(e -> action.run());
        }
        
        return btn;
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
    
    public Staff getStaff() {
        return staff;
    }
}
