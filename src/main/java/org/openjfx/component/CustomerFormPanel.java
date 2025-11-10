package org.openjfx.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openjfx.util.IconUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

/**
 * Form component for guest checkout
 * Collects customer name, email, and phone
 */
public class CustomerFormPanel extends VBox {
    
    private TextField nameField;
    private TextField emailField;
    private TextField phoneField;
    
    private Label nameError;
    private Label emailError;
    private Label phoneError;
    
    public CustomerFormPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white;");
        
        // Header
        Label headerLabel = new Label("Customer Information");
        headerLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        Label subLabel = new Label("Please enter your contact details for booking confirmation");
        subLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        // Form grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20, 0, 0, 0));
        
        // Name field
        VBox nameBox = createFormField(
            "Full Name",
            "Enter your full name",
            FontAwesomeIcon.USER
        );
        nameField = (TextField) ((HBox) nameBox.getChildren().get(1)).getChildren().get(1);
        nameError = new Label();
        nameError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        nameError.setVisible(false);
        nameBox.getChildren().add(nameError);
        
        // Email field
        VBox emailBox = createFormField(
            "Email Address",
            "your.email@example.com",
            FontAwesomeIcon.ENVELOPE
        );
        emailField = (TextField) ((HBox) emailBox.getChildren().get(1)).getChildren().get(1);
        emailError = new Label();
        emailError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        emailError.setVisible(false);
        emailBox.getChildren().add(emailError);
        
        // Phone field
        VBox phoneBox = createFormField(
            "Phone Number",
            "08xx-xxxx-xxxx",
            FontAwesomeIcon.PHONE
        );
        phoneField = (TextField) ((HBox) phoneBox.getChildren().get(1)).getChildren().get(1);
        phoneError = new Label();
        phoneError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        phoneError.setVisible(false);
        phoneBox.getChildren().add(phoneError);
        
        formGrid.add(nameBox, 0, 0);
        formGrid.add(emailBox, 0, 1);
        formGrid.add(phoneBox, 0, 2);
        
        // Info box
        HBox infoBox = new HBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle(
            "-fx-background-color: #e3f2fd;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #2196F3;" +
            "-fx-border-radius: 5px;" +
            "-fx-border-width: 1px;"
        );
        
        Label infoIcon = new Label();
        infoIcon.setGraphic(IconUtil.createIcon(FontAwesomeIcon.INFO_CIRCLE, 16));
        infoIcon.setStyle("-fx-text-fill: #2196F3;");
        
        Label infoText = new Label(
            "Your information will be used to send booking confirmation and event updates."
        );
        infoText.setWrapText(true);
        infoText.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #1976D2;"
        );
        
        infoBox.getChildren().addAll(infoIcon, infoText);
        
        getChildren().addAll(headerLabel, subLabel, formGrid, infoBox);
    }
    
    private VBox createFormField(String label, String placeholder, FontAwesomeIcon icon) {
        VBox fieldBox = new VBox(8);
        
        // Label
        Label fieldLabel = new Label(label);
        fieldLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        // Input with icon
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        inputBox.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-radius: 5px;" +
            "-fx-border-width: 1px;" +
            "-fx-padding: 10px 15px;"
        );
        
        Label iconLabel = new Label();
        iconLabel.setGraphic(IconUtil.createIcon(icon, 16));
        iconLabel.setStyle("-fx-text-fill: #6c757d;");
        
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 13px;"
        );
        HBox.setHgrow(textField, javafx.scene.layout.Priority.ALWAYS);
        
        // Focus effect
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                inputBox.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-border-color: #2196F3;" +
                    "-fx-border-radius: 5px;" +
                    "-fx-border-width: 2px;" +
                    "-fx-padding: 9px 14px;"
                );
            } else {
                inputBox.setStyle(
                    "-fx-background-color: #f8f9fa;" +
                    "-fx-background-radius: 5px;" +
                    "-fx-border-color: #dee2e6;" +
                    "-fx-border-radius: 5px;" +
                    "-fx-border-width: 1px;" +
                    "-fx-padding: 10px 15px;"
                );
            }
        });
        
        inputBox.getChildren().addAll(iconLabel, textField);
        fieldBox.getChildren().addAll(fieldLabel, inputBox);
        
        return fieldBox;
    }
    
    /**
     * Validate all fields
     */
    public boolean validate() {
        boolean isValid = true;
        
        // Reset errors
        nameError.setVisible(false);
        emailError.setVisible(false);
        phoneError.setVisible(false);
        
        // Validate name
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            nameError.setText("Name is required");
            nameError.setVisible(true);
            isValid = false;
        } else if (name.length() < 3) {
            nameError.setText("Name must be at least 3 characters");
            nameError.setVisible(true);
            isValid = false;
        }
        
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("Email is required");
            emailError.setVisible(true);
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailError.setText("Please enter a valid email address");
            emailError.setVisible(true);
            isValid = false;
        }
        
        // Validate phone
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            phoneError.setText("Phone number is required");
            phoneError.setVisible(true);
            isValid = false;
        } else if (!isValidPhone(phone)) {
            phoneError.setText("Please enter a valid phone number (min 10 digits)");
            phoneError.setVisible(true);
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private boolean isValidPhone(String phone) {
        // Remove common separators
        String cleaned = phone.replaceAll("[\\s\\-()]", "");
        // Check if contains at least 10 digits
        return cleaned.matches("\\d{10,}");
    }
    
    // Getters
    public String getCustomerName() {
        return nameField.getText().trim();
    }
    
    public String getCustomerEmail() {
        return emailField.getText().trim();
    }
    
    public String getCustomerPhone() {
        return phoneField.getText().trim();
    }
    
    // Setters for pre-filling
    public void setCustomerName(String name) {
        nameField.setText(name);
    }
    
    public void setCustomerEmail(String email) {
        emailField.setText(email);
    }
    
    public void setCustomerPhone(String phone) {
        phoneField.setText(phone);
    }
    
    public void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        nameError.setVisible(false);
        emailError.setVisible(false);
        phoneError.setVisible(false);
    }
}
