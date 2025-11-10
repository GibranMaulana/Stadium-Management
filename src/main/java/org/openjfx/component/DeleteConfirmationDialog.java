package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.openjfx.model.Event;
import org.openjfx.service.EventService;

/**
 * Delete Confirmation Dialog Component
 */
public class DeleteConfirmationDialog {
    
    private final Stage dialog;
    private final Event event;
    private final Runnable onSuccess;
    private final EventService eventService;
    private Label warningMsg;
    
    public DeleteConfirmationDialog(Stage owner, Event event, Runnable onSuccess) {
        this.dialog = new Stage();
        this.event = event;
        this.onSuccess = onSuccess;
        this.eventService = new EventService();
        
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Confirm Delete");
        dialog.setResizable(false);
        
        createDialog();
    }
    
    private void createDialog() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white;");

        FontAwesomeIconView warningIcon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
        warningIcon.setSize("48");
        warningIcon.setFill(javafx.scene.paint.Color.web("#e74c3c"));

        Label message = new Label("Are you sure you want to delete this event?");
        message.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label eventInfo = new Label(event.getEventName() + " - " + event.getEventDate());
        eventInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        warningMsg = new Label();
        if (eventService.hasBookings(event.getId())) {
            warningMsg.setText("Warning: This event has existing bookings!");
            warningMsg.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; " +
                          "-fx-padding: 8 20; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleDelete());

        buttonBox.getChildren().addAll(cancelBtn, deleteBtn);

        content.getChildren().addAll(warningIcon, message, eventInfo, warningMsg, buttonBox);

        Scene scene = new Scene(content, 400, 250);
        dialog.setScene(scene);
    }
    
    private void handleDelete() {
        if (eventService.deleteEvent(event.getId())) {
            dialog.close();
            onSuccess.run();
        } else {
            warningMsg.setText("Cannot delete event with existing bookings!");
            warningMsg.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
