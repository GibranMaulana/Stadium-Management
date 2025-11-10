package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.HBox;
import org.openjfx.model.Event;

import java.util.List;
import java.util.function.Consumer;

/**
 * Event Table Component
 * Displays events in a table with edit/delete actions
 */
public class EventTableView extends TableView<Event> {
    
    public EventTableView(Consumer<Event> onEdit, Consumer<Event> onDelete) {
        super();
        setStyle("-fx-background-color: white; -fx-font-size: 13px;");
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        setupColumns(onEdit, onDelete);
    }
    
    private void setupColumns(Consumer<Event> onEdit, Consumer<Event> onDelete) {
        TableColumn<Event, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        idCol.setPrefWidth(50);

        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEventName()));
        nameCol.setPrefWidth(200);

        TableColumn<Event, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEventType()));
        typeCol.setPrefWidth(120);

        TableColumn<Event, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEventDate().toString()));
        dateCol.setPrefWidth(100);

        TableColumn<Event, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEventTime().toString()));
        timeCol.setPrefWidth(80);

        TableColumn<Event, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    String color;
                    switch (status) {
                        case "UPCOMING":
                            color = "#3498db";
                            break;
                        case "ONGOING":
                            color = "#27ae60";
                            break;
                        case "COMPLETED":
                            color = "#95a5a6";
                            break;
                        case "CANCELLED":
                            color = "#e74c3c";
                            break;
                        default:
                            color = "#7f8c8d";
                            break;
                    }
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Event, String> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getBookedSeats() + "/" + data.getValue().getTotalSeats()));
        seatsCol.setPrefWidth(80);

        TableColumn<Event, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actionBox = new HBox(5);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; " +
                               "-fx-padding: 5 10; -fx-cursor: hand;");
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
                editIcon.setSize("12");
                editIcon.setFill(javafx.scene.paint.Color.WHITE);
                editBtn.setGraphic(editIcon);

                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; " +
                                 "-fx-padding: 5 10; -fx-cursor: hand;");
                FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                deleteIcon.setSize("12");
                deleteIcon.setFill(javafx.scene.paint.Color.WHITE);
                deleteBtn.setGraphic(deleteIcon);

                actionBox.getChildren().addAll(editBtn, deleteBtn);
                actionBox.setAlignment(Pos.CENTER);
                
                editBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    onEdit.accept(event);
                });

                deleteBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    onDelete.accept(event);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        getColumns().addAll(idCol, nameCol, typeCol, dateCol, timeCol, statusCol, seatsCol, actionsCol);
    }
    
    public void loadEvents(List<Event> events) {
        getItems().setAll(events);
    }
}
