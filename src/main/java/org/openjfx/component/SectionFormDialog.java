package org.openjfx.component;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import org.openjfx.model.Section;

/**
 * Dialog for editing section details
 */
public class SectionFormDialog extends Dialog<Section> {
    
    private final TextField nameField;
    private final TextField rowsField;
    private final TextField seatsPerRowField;
    private final Label capacityLabel;
    private final Label typeLabel;
    private final Section section;
    
    public SectionFormDialog(Section section) {
        this.section = section;
        
        setTitle("Edit Section");
        setHeaderText("Edit section details for " + section.getSectionName());
        
        // Make dialog modal
        initModality(Modality.APPLICATION_MODAL);
        
        // Create form fields
        nameField = new TextField(section.getSectionName());
        nameField.setPromptText("Section Name");
        
        rowsField = new TextField(String.valueOf(section.getTotalRows()));
        rowsField.setPromptText("Total Rows");
        
        seatsPerRowField = new TextField(String.valueOf(section.getSeatsPerRow()));
        seatsPerRowField.setPromptText("Seats Per Row");
        
        // Type label (read-only)
        typeLabel = new Label(section.getSectionType());
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Capacity label (auto-calculated)
        capacityLabel = new Label();
        updateCapacityLabel();
        capacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        // Add listeners to recalculate capacity
        rowsField.textProperty().addListener((obs, old, newVal) -> updateCapacityLabel());
        seatsPerRowField.textProperty().addListener((obs, old, newVal) -> updateCapacityLabel());
        
        // Only allow editing rows/seats for tribune sections
        if (section.getSectionType().equals("FIELD")) {
            rowsField.setDisable(true);
            seatsPerRowField.setDisable(true);
            rowsField.setPromptText("N/A (Standing Area)");
            seatsPerRowField.setPromptText("N/A (Standing Area)");
        }
        
        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");
        
        // Add fields to grid
        int row = 0;
        
        grid.add(new Label("Section Name:"), 0, row);
        grid.add(nameField, 1, row);
        row++;
        
        grid.add(new Label("Section Type:"), 0, row);
        grid.add(typeLabel, 1, row);
        row++;
        
        grid.add(new Label("Total Rows:"), 0, row);
        grid.add(rowsField, 1, row);
        row++;
        
        grid.add(new Label("Seats Per Row:"), 0, row);
        grid.add(seatsPerRowField, 1, row);
        row++;
        
        grid.add(new Label("Total Capacity:"), 0, row);
        grid.add(capacityLabel, 1, row);
        row++;
        
        // Make fields fill width
        nameField.setPrefWidth(250);
        rowsField.setPrefWidth(250);
        seatsPerRowField.setPrefWidth(250);
        
        // Add validation message
        Label validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        grid.add(validationLabel, 0, row, 2, 1);
        
        getDialogPane().setContent(grid);
        
        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // Style buttons
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 8 20;");
        
        // Add validation
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateInput(validationLabel)) {
                event.consume(); // Prevent dialog from closing
            }
        });
        
        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType && validateInput(validationLabel)) {
                return createUpdatedSection();
            }
            return null;
        });
    }
    
    /**
     * Update the capacity label based on rows and seats per row
     */
    private void updateCapacityLabel() {
        try {
            if (section.getSectionType().equals("FIELD")) {
                capacityLabel.setText("Flexible (Standing Area)");
            } else {
                int rows = Integer.parseInt(rowsField.getText().trim());
                int seatsPerRow = Integer.parseInt(seatsPerRowField.getText().trim());
                int capacity = rows * seatsPerRow;
                capacityLabel.setText(String.format("%,d seats", capacity));
            }
        } catch (NumberFormatException e) {
            capacityLabel.setText("Invalid input");
            capacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            return;
        }
        capacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
    }
    
    /**
     * Validate input fields
     */
    private boolean validateInput(Label validationLabel) {
        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            validationLabel.setText("⚠ Section name is required");
            nameField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            return false;
        } else {
            nameField.setStyle("");
        }
        
        // For tribune sections, validate rows and seats
        if (section.getSectionType().equals("TRIBUNE")) {
            try {
                int rows = Integer.parseInt(rowsField.getText().trim());
                if (rows < 1 || rows > 50) {
                    validationLabel.setText("⚠ Total rows must be between 1 and 50");
                    rowsField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                    return false;
                } else {
                    rowsField.setStyle("");
                }
            } catch (NumberFormatException e) {
                validationLabel.setText("⚠ Total rows must be a valid number");
                rowsField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                return false;
            }
            
            try {
                int seatsPerRow = Integer.parseInt(seatsPerRowField.getText().trim());
                if (seatsPerRow < 1 || seatsPerRow > 100) {
                    validationLabel.setText("⚠ Seats per row must be between 1 and 100");
                    seatsPerRowField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                    return false;
                } else {
                    seatsPerRowField.setStyle("");
                }
            } catch (NumberFormatException e) {
                validationLabel.setText("⚠ Seats per row must be a valid number");
                seatsPerRowField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                return false;
            }
        }
        
        validationLabel.setText("");
        return true;
    }
    
    /**
     * Create updated Section object with form values
     */
    private Section createUpdatedSection() {
        int rows = section.getSectionType().equals("TRIBUNE") ? 
                   Integer.parseInt(rowsField.getText().trim()) : 0;
        int seatsPerRow = section.getSectionType().equals("TRIBUNE") ? 
                          Integer.parseInt(seatsPerRowField.getText().trim()) : 0;
        int capacity = rows * seatsPerRow;
        
        return new Section(
            section.getSectionId(),
            nameField.getText().trim(),
            section.getSectionType(),
            rows,
            seatsPerRow,
            capacity
        );
    }
}
