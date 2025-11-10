package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * A card component that displays a statistic with an icon, title, and value.
 * Features hover animations and color-coded styling.
 */
public class StatCard extends VBox {
    
    private static final String BASE_STYLE = 
        "-fx-background-color: white; " +
        "-fx-background-radius: 12; " +
        "-fx-border-color: #e0e0e0; " +
        "-fx-border-radius: 12; " +
        "-fx-border-width: 1; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);";
    
    private static final String HOVER_STYLE = 
        "-fx-background-color: white; " +
        "-fx-background-radius: 12; " +
        "-fx-border-color: %s; " +
        "-fx-border-radius: 12; " +
        "-fx-border-width: 2; " +
        "-fx-effect: dropshadow(gaussian, %s, 12, 0, 0, 4);";
    
    private FontAwesomeIconView iconView;
    private Label titleLabel;
    private Label valueLabel;
    private Label subtitleLabel;
    private String accentColor;
    
    /**
     * Creates a StatCard with icon, title, value, and accent color
     * 
     * @param icon FontAwesome icon to display
     * @param title The title/label of the statistic
     * @param value The main value to display
     * @param accentColor Hex color for the icon and accents (e.g., "#3498db")
     */
    public StatCard(FontAwesomeIcon icon, String title, String value, String accentColor) {
        this(icon, title, value, null, accentColor);
    }
    
    /**
     * Creates a StatCard with icon, title, value, subtitle, and accent color
     * 
     * @param icon FontAwesome icon to display
     * @param title The title/label of the statistic
     * @param value The main value to display
     * @param subtitle Optional subtitle/description
     * @param accentColor Hex color for the icon and accents (e.g., "#3498db")
     */
    public StatCard(FontAwesomeIcon icon, String title, String value, String subtitle, String accentColor) {
        super(12);
        this.accentColor = accentColor;
        
        initializeUI(icon, title, value, subtitle);
        applyStyles();
        setupAnimations();
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeUI(FontAwesomeIcon icon, String title, String value, String subtitle) {
        // Icon with colored background
        HBox iconContainer = new HBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setPrefSize(50, 50);
        iconContainer.setMaxSize(50, 50);
        iconContainer.setStyle(
            "-fx-background-color: " + accentColor + "20; " +
            "-fx-background-radius: 10;"
        );
        
        iconView = new FontAwesomeIconView(icon);
        iconView.setSize("24");
        iconView.setStyle("-fx-fill: " + accentColor + ";");
        
        iconContainer.getChildren().add(iconView);
        
        // Title label
        titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        titleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Value label
        valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Subtitle label (optional)
        if (subtitle != null && !subtitle.isEmpty()) {
            subtitleLabel = new Label(subtitle);
            subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
            subtitleLabel.setStyle("-fx-text-fill: #95a5a6;");
            subtitleLabel.setWrapText(true);
        }
        
        // Layout
        VBox contentBox = new VBox(6);
        contentBox.getChildren().addAll(titleLabel, valueLabel);
        if (subtitleLabel != null) {
            contentBox.getChildren().add(subtitleLabel);
        }
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        
        this.getChildren().addAll(iconContainer, contentBox);
    }
    
    /**
     * Apply base styling to the card
     */
    private void applyStyles() {
        this.setStyle(BASE_STYLE);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.TOP_LEFT);
        this.setPrefWidth(240);
        this.setMinWidth(200);
        this.setMaxWidth(280);
        this.setPrefHeight(140);
        this.setCursor(Cursor.HAND);
    }
    
    /**
     * Setup hover animations
     */
    private void setupAnimations() {
        // Scale animation
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), this);
        scaleUp.setToX(1.03);
        scaleUp.setToY(1.03);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), this);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        // Hover effects
        this.setOnMouseEntered(e -> {
            this.setStyle(String.format(HOVER_STYLE, accentColor, accentColor + "40"));
            scaleUp.play();
        });
        
        this.setOnMouseExited(e -> {
            this.setStyle(BASE_STYLE);
            scaleDown.play();
        });
    }
    
    /**
     * Update the displayed value
     * 
     * @param newValue The new value to display
     */
    public void updateValue(String newValue) {
        // Fade animation for value change
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), valueLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), valueLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        fadeOut.setOnFinished(e -> {
            valueLabel.setText(newValue);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    /**
     * Update the title
     * 
     * @param newTitle The new title to display
     */
    public void updateTitle(String newTitle) {
        titleLabel.setText(newTitle);
    }
    
    /**
     * Update the subtitle
     * 
     * @param newSubtitle The new subtitle to display
     */
    public void updateSubtitle(String newSubtitle) {
        if (subtitleLabel != null) {
            subtitleLabel.setText(newSubtitle);
        } else {
            // Create subtitle if it doesn't exist
            subtitleLabel = new Label(newSubtitle);
            subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
            subtitleLabel.setStyle("-fx-text-fill: #95a5a6;");
            subtitleLabel.setWrapText(true);
            
            // Add to the content box (second child after icon container)
            if (this.getChildren().size() > 1) {
                VBox contentBox = (VBox) this.getChildren().get(1);
                contentBox.getChildren().add(subtitleLabel);
            }
        }
    }
    
    /**
     * Update the icon
     * 
     * @param newIcon The new FontAwesome icon
     */
    public void updateIcon(FontAwesomeIcon newIcon) {
        iconView.setIcon(newIcon);
    }
    
    /**
     * Get the current value
     */
    public String getValue() {
        return valueLabel.getText();
    }
    
    /**
     * Get the current title
     */
    public String getTitle() {
        return titleLabel.getText();
    }
    
    /**
     * Legacy method for backward compatibility
     * @deprecated Use updateValue() instead
     */
    @Deprecated
    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
