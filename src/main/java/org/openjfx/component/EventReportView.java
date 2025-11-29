package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.openjfx.model.Booking;
import org.openjfx.model.BookingSeat;
import org.openjfx.model.Event;
import org.openjfx.model.InventoryPurchase;
import org.openjfx.service.BookingService;
import org.openjfx.service.EventService;
import org.openjfx.service.EventExpenseService;
import org.openjfx.service.InventoryService;
import org.openjfx.service.InventoryPurchaseService;
import org.openjfx.model.EventExpense;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventReportView
 * Left: list of events
 * Right: per-event summary, section breakdown, revenue/expense/profit
 */
public class EventReportView extends BorderPane {

    private final EventService eventService = new EventService();
    private final BookingService bookingService = new BookingService();
    // reportService not currently required by this view; keep purchaseService and other services
    private final InventoryPurchaseService purchaseService = new InventoryPurchaseService();
    private final EventExpenseService eventExpenseService = new EventExpenseService();
    private final InventoryService inventoryService = new InventoryService();

    private final ListView<Event> eventsList = new ListView<>();
    private final DatePicker startDate = new DatePicker();
    private final DatePicker endDate = new DatePicker();

    private final TableView<EventDetailRow> eventDetailsTable = new TableView<>();
    private final ListView<String> expensesList = new ListView<>();

    public EventReportView() {
        setPadding(new Insets(0));

        // Left: events list and period filter (styled card)
        VBox left = new VBox(15);
        left.setPrefWidth(320);
        left.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        left.setPadding(new Insets(20));

        Label eventsLabel = new Label("Event List");
        eventsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        eventsLabel.setStyle("-fx-text-fill: #2c3e50;");

        eventsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getEventName() + " (" + item.getEventDate() + ")");
            }
        });

        eventsList.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) loadReportForEvent(sel);
        });
        VBox.setVgrow(eventsList, Priority.ALWAYS);

        // Period filter box
        VBox periodBox = new VBox(10);
        periodBox.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 5; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-radius: 5; " +
            "-fx-border-width: 1;"
        );
        periodBox.setPadding(new Insets(12));

        Label periodLabel = new Label("Period Filter");
        periodLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        periodLabel.setStyle("-fx-text-fill: #495057;");

        HBox dateBox = new HBox(8);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.getChildren().addAll(new Label("From:"), startDate, new Label("To:"), endDate);

        Button applyBtn = new Button("Apply");
        applyBtn.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 6 12; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;"
        );
        applyBtn.setOnAction(e -> {
            loadEvents(); // Reload list dengan filter period baru
        });

        periodBox.getChildren().addAll(periodLabel, dateBox, applyBtn);

        left.getChildren().addAll(eventsLabel, eventsList, periodBox);

        // Right: report summary (styled card)
        VBox right = new VBox(15);
        right.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        right.setPadding(new Insets(20));
        HBox.setHgrow(right, Priority.ALWAYS);

        Label detailsLabel = new Label("Event Details");
        detailsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        detailsLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Event details table with attributes
        TableColumn<EventDetailRow, String> colAttr = new TableColumn<>("Attribute");
        colAttr.setCellValueFactory(data -> data.getValue().attributeProperty());
        colAttr.setPrefWidth(180);

        TableColumn<EventDetailRow, String> colValue = new TableColumn<>("Value");
        colValue.setCellValueFactory(data -> data.getValue().valueProperty());
        colValue.setPrefWidth(300);

        eventDetailsTable.getColumns().add(colAttr);
        eventDetailsTable.getColumns().add(colValue);
        eventDetailsTable.setPrefHeight(220);
        VBox.setVgrow(eventDetailsTable, Priority.ALWAYS);

        // Add Expense button with icon
        Button btnAddExpense = new Button("Add Expense");
        FontAwesomeIconView expenseIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE);
        expenseIcon.setSize("14");
        expenseIcon.setFill(javafx.scene.paint.Color.WHITE);
        btnAddExpense.setGraphic(expenseIcon);
        btnAddExpense.setStyle(
            "-fx-background-color: #27ae60; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        btnAddExpense.setOnAction(e -> {
            Event sel = eventsList.getSelectionModel().getSelectedItem();
            if (sel != null) showAddExpenseDialog(sel);
        });

        // Expenses box
        VBox expensesBox = new VBox(10);
        expensesBox.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 5; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-radius: 5; " +
            "-fx-border-width: 1;"
        );
        expensesBox.setPadding(new Insets(12));
        expensesBox.setPrefHeight(180);

        Label expensesLabel = new Label("Recent Expenses");
        expensesLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        expensesLabel.setStyle("-fx-text-fill: #495057;");

        VBox.setVgrow(expensesList, Priority.ALWAYS);
        expensesBox.getChildren().addAll(expensesLabel, expensesList);

        right.getChildren().addAll(detailsLabel, eventDetailsTable, btnAddExpense, expensesBox);

        HBox mainContent = new HBox(20);
        mainContent.getChildren().addAll(left, right);

        setCenter(mainContent);

        loadEvents();
    }

    private void loadEvents() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        List<Event> events = eventService.getAllEvents();
        
        // Filter by period if selected
        if (start != null || end != null) {
            events.removeIf(ev -> {
                LocalDate eventDate = ev.getEventDate();
                if (eventDate == null) return true; // exclude if no date
                if (start != null && eventDate.isBefore(start)) return true;
                if (end != null && eventDate.isAfter(end)) return true;
                return false;
            });
        }

        ObservableList<Event> items = FXCollections.observableArrayList(events);
        eventsList.setItems(items);
        if (!items.isEmpty()) eventsList.getSelectionModel().selectFirst();
    }

    private void loadReportForEvent(Event event) {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        List<Booking> bookings = bookingService.getBookingsByEvent(event.getId());

        // Filter by date range if provided
        if (start != null || end != null) {
            bookings.removeIf(b -> {
                java.sql.Timestamp ts = b.getBookingDate();
                if (ts == null) return true;
                LocalDate d = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (start != null && d.isBefore(start)) return true;
                if (end != null && d.isAfter(end)) return true;
                return false;
            });
        }

        // Filter only CONFIRMED bookings for visitor count
        bookings.removeIf(b -> !"CONFIRMED".equals(b.getBookingStatus()));

        int totalBookings = bookings.size();
        int totalTickets = 0;
        double totalRevenue = 0.0;

        for (Booking b : bookings) {
            List<BookingSeat> seats = bookingService.getBookingSeats(b.getBookingId());
            for (BookingSeat s : seats) {
                // Count all seats from CONFIRMED bookings (not just ACTIVE status)
                totalTickets++;
                totalRevenue += s.getPrice();
            }
        }

        // Expenses: include both inventory purchases and event-level expenses
        double totalExpenses = 0.0;
        if (start != null && end != null) {
            List<InventoryPurchase> purchases = purchaseService.getPurchasesInPeriod(start, end);
            for (InventoryPurchase p : purchases) {
                if (p.getEventId() != null && p.getEventId() == event.getId()) totalExpenses += p.getTotalCost();
            }
            // include event expenses in the period for this event
            totalExpenses += eventExpenseService.getTotalExpensesForEventInPeriod(start, end, event.getId());
        } else {
            totalExpenses = purchaseService.getTotalExpensesForEvent(event.getId()) + eventExpenseService.getTotalExpensesForEvent(event.getId());
        }

        // Populate event details table
        ObservableList<EventDetailRow> rows = FXCollections.observableArrayList(
            new EventDetailRow("Event Name", event.getEventName() != null ? event.getEventName() : "-"),
            new EventDetailRow("Event Date", event.getEventDate() != null ? event.getEventDate().toString() : "-"),
            new EventDetailRow("Event Time", event.getEventTime() != null ? event.getEventTime().toString() : "-"),
            new EventDetailRow("Event Type", event.getEventType() != null ? event.getEventType() : "-"),
            new EventDetailRow("Visitor Count", String.valueOf(totalTickets))
        );
        eventDetailsTable.setItems(rows);

        // load recent expenses for this event
        loadEventExpenses(event.getId());
    }

    // Simple row class for event details table
    public static class EventDetailRow {
        private final javafx.beans.property.SimpleStringProperty attribute;
        private final javafx.beans.property.SimpleStringProperty value;

        public EventDetailRow(String attribute, String value) {
            this.attribute = new javafx.beans.property.SimpleStringProperty(attribute);
            this.value = new javafx.beans.property.SimpleStringProperty(value);
        }

        public String getAttribute() { return attribute.get(); }
        public javafx.beans.property.StringProperty attributeProperty() { return attribute; }

        public String getValue() { return value.get(); }
        public javafx.beans.property.StringProperty valueProperty() { return value; }
    }

    private void loadEventExpenses(int eventId) {
        List<EventExpense> list = eventExpenseService.getExpensesForEvent(eventId);
        ObservableList<String> items = FXCollections.observableArrayList();
        for (EventExpense e : list) {
            String notes = e.getNotes() == null ? "" : e.getNotes();
            String formatted = String.format("[%s] Rp %,.2f - %s", e.getExpenseType(), e.getTotalCost(), notes);
            items.add(formatted);
        }
        expensesList.setItems(items);
    }

    private void showAddExpenseDialog(Event event) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Add Expense for " + event.getEventName());
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        // Fields
        ChoiceBox<String> typeBox = new ChoiceBox<>(FXCollections.observableArrayList("INVENTORY_DAMAGE", "OPERATIONAL"));
        typeBox.getSelectionModel().selectFirst();

        ComboBox<org.openjfx.model.InventoryItem> itemBox = new ComboBox<>();
        itemBox.setPrefWidth(300);
        List<org.openjfx.model.InventoryItem> all = inventoryService.getAllItems();
        itemBox.setItems(FXCollections.observableArrayList(all));
        itemBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(org.openjfx.model.InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getItemName() + " (" + item.getQuantity() + ")");
            }
        });
        itemBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(org.openjfx.model.InventoryItem object) { return object == null ? "" : object.getItemName(); }
            @Override public org.openjfx.model.InventoryItem fromString(String string) { return null; }
        });

        Spinner<Integer> qty = new Spinner<>(1, 10000, 1);
        TextField unitCost = new TextField();
        unitCost.setPromptText("Unit cost (Rp)");
        TextField totalCostField = new TextField();
        totalCostField.setPromptText("Total cost (Rp) - optional, overrides unit*qty if filled");

        // when item selected, prefill unit cost
        itemBox.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                double price = newItem.getUnitPrice();
                unitCost.setText(String.format("%,.2f", price));
            }
        });
        TextArea notes = new TextArea();
        notes.setPrefRowCount(3);

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8);
        gp.add(new Label("Type:"), 0, 0); gp.add(typeBox, 1, 0);
        gp.add(new Label("Item (optional):"), 0, 1); gp.add(itemBox, 1, 1);
        gp.add(new Label("Quantity:"), 0, 2); gp.add(qty, 1, 2);
        gp.add(new Label("Unit Cost (Rp):"), 0, 3); gp.add(unitCost, 1, 3);
        gp.add(new Label("Total Cost (Rp):"), 0, 5); gp.add(totalCostField, 1, 5);
        gp.add(new Label("Notes:"), 0, 4); gp.add(notes, 1, 4);

        dlg.getDialogPane().setContent(gp);

        dlg.setResultConverter(btn -> {
            if (btn == save) {
                String type = typeBox.getValue();
                org.openjfx.model.InventoryItem selItem = itemBox.getValue();
                Integer itemId = selItem == null ? null : selItem.getItemId();
                int q = qty.getValue();
                double uc = 0.0;
                try { uc = Double.parseDouble(unitCost.getText().replaceAll(",", "")); } catch (Exception ex) { uc = 0.0; }
                double total = 0.0;
                String totalTxt = totalCostField.getText();
                if (totalTxt != null && !totalTxt.trim().isEmpty()) {
                    try { total = Double.parseDouble(totalTxt.replaceAll(",", "")); } catch (Exception ex) { total = 0.0; }
                } else {
                    total = uc * q;
                }

                if (total <= 0.0) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Total cost must be greater than 0.", ButtonType.OK);
                    a.showAndWait();
                    return null;
                }

                org.openjfx.model.EventExpense e = new org.openjfx.model.EventExpense();
                e.setEventId(event.getId());
                e.setExpenseType(type);
                if (itemId != null) e.setItemId(itemId);
                e.setQuantity(q);
                e.setUnitCost(uc);
                e.setTotalCost(total);
                e.setNotes(notes.getText());

                boolean ok = eventExpenseService.addExpense(e);
                if (ok) {
                    if ("INVENTORY_DAMAGE".equals(type) && selItem != null) {
                        inventoryService.decreaseQuantity(selItem.getItemId(), q);
                    }
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Expense recorded successfully.", ButtonType.OK);
                    a.showAndWait();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to save expense. See logs for details.", ButtonType.OK);
                    a.showAndWait();
                }

                // reload report and expenses
                loadEventExpenses(event.getId());
                loadReportForEvent(event);
            }
            return null;
        });

        dlg.showAndWait();
    }
}
