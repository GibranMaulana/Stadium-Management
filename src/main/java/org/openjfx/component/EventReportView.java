package org.openjfx.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import org.openjfx.service.InventoryPurchaseService;

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

    private final ListView<Event> eventsList = new ListView<>();
    private final DatePicker startDate = new DatePicker();
    private final DatePicker endDate = new DatePicker();

    private final Label lblEventName = new Label();
    private final Label lblEventDate = new Label();
    private final Label lblBookings = new Label();
    private final Label lblTickets = new Label();
    private final Label lblRevenue = new Label();
    private final Label lblExpenses = new Label();
    private final Label lblProfit = new Label();

    private final TableView<SectionRow> sectionTable = new TableView<>();

    public EventReportView() {
        setPadding(new Insets(20));

        HBox top = new HBox(12);
        Label title = new Label("Event Reports");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        top.getChildren().add(title);
        setTop(top);

        // Left: events list and period filter
        VBox left = new VBox(10);
        left.setPadding(new Insets(10));
        left.setPrefWidth(300);

        Label eventsLabel = new Label("Events");
        eventsLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));

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

        HBox dateBox = new HBox(8, new Label("From:"), startDate, new Label("To:"), endDate);
        Button applyBtn = new Button("Apply Period");
        applyBtn.setOnAction(e -> {
            Event sel = eventsList.getSelectionModel().getSelectedItem();
            if (sel != null) loadReportForEvent(sel);
        });

        left.getChildren().addAll(eventsLabel, eventsList, dateBox, applyBtn);

        // Right: report summary
        VBox right = new VBox(12);
        right.setPadding(new Insets(10));

        lblEventName.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblEventDate.setStyle("-fx-text-fill: #7f8c8d;");

        GridPane summary = new GridPane();
        summary.setHgap(12);
        summary.setVgap(8);
        summary.add(new Label("Bookings:"), 0, 0);
        summary.add(lblBookings, 1, 0);
        summary.add(new Label("Tickets Sold:"), 0, 1);
        summary.add(lblTickets, 1, 1);
        summary.add(new Label("Revenue (Rp):"), 0, 2);
        summary.add(lblRevenue, 1, 2);
        summary.add(new Label("Expenses (Rp):"), 0, 3);
        summary.add(lblExpenses, 1, 3);
        summary.add(new Label("Profit (Rp):"), 0, 4);
        summary.add(lblProfit, 1, 4);

        // Section table
        TableColumn<SectionRow, String> colSection = new TableColumn<>("Section");
        colSection.setCellValueFactory(data -> data.getValue().sectionTitleProperty());
        colSection.setPrefWidth(220);

        TableColumn<SectionRow, Integer> colTickets = new TableColumn<>("Tickets Sold");
        colTickets.setCellValueFactory(data -> data.getValue().ticketsProperty().asObject());
        colTickets.setPrefWidth(120);

        TableColumn<SectionRow, Double> colRev = new TableColumn<>("Revenue (Rp)");
        colRev.setCellValueFactory(data -> data.getValue().revenueProperty().asObject());
        colRev.setPrefWidth(140);

        sectionTable.getColumns().add(colSection);
        sectionTable.getColumns().add(colTickets);
        sectionTable.getColumns().add(colRev);

        right.getChildren().addAll(lblEventName, lblEventDate, summary, new Label("Breakdown by Section"), sectionTable);

        setLeft(left);
        setCenter(right);

        loadEvents();
    }

    private void loadEvents() {
        List<Event> events = eventService.getAllEvents();
        ObservableList<Event> items = FXCollections.observableArrayList(events);
        eventsList.setItems(items);
        if (!items.isEmpty()) eventsList.getSelectionModel().selectFirst();
    }

    private void loadReportForEvent(Event event) {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        lblEventName.setText(event.getEventName());
        lblEventDate.setText(event.getEventDate() + " " + event.getEventTime());

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

        int totalBookings = bookings.size();
        int totalTickets = 0;
        double totalRevenue = 0.0;

        Map<String, SectionRow> sections = new HashMap<>();

        for (Booking b : bookings) {
            List<BookingSeat> seats = bookingService.getBookingSeats(b.getBookingId());
            for (BookingSeat s : seats) {
                if (!s.isActive()) continue;
                totalTickets++;
                totalRevenue += s.getPrice();
                String title = s.getSectionTitle() != null ? s.getSectionTitle() : "Standing";
                SectionRow row = sections.computeIfAbsent(title, k -> new SectionRow(k, 0, 0.0));
                row.setTickets(row.getTickets() + 1);
                row.setRevenue(row.getRevenue() + s.getPrice());
            }
        }

        // Expenses: if period specified, sum purchases in period attributed to this event; otherwise total for event
        double totalExpenses = 0.0;
        if (start != null && end != null) {
            List<InventoryPurchase> purchases = purchaseService.getPurchasesInPeriod(start, end);
            for (InventoryPurchase p : purchases) {
                if (p.getEventId() != null && p.getEventId() == event.getId()) totalExpenses += p.getTotalCost();
            }
        } else {
            totalExpenses = purchaseService.getTotalExpensesForEvent(event.getId());
        }

        double profit = totalRevenue - totalExpenses;

        lblBookings.setText(String.valueOf(totalBookings));
        lblTickets.setText(String.valueOf(totalTickets));
        lblRevenue.setText(String.format("%,.2f", totalRevenue));
        lblExpenses.setText(String.format("%,.2f", totalExpenses));
        lblProfit.setText(String.format("%,.2f", profit));

        ObservableList<SectionRow> rows = FXCollections.observableArrayList(sections.values());
        sectionTable.setItems(rows);
    }

    // Simple row class for section table
    public static class SectionRow {
        private final javafx.beans.property.SimpleStringProperty sectionTitle;
        private final javafx.beans.property.SimpleIntegerProperty tickets;
        private final javafx.beans.property.SimpleDoubleProperty revenue;

        public SectionRow(String sectionTitle, int tickets, double revenue) {
            this.sectionTitle = new javafx.beans.property.SimpleStringProperty(sectionTitle);
            this.tickets = new javafx.beans.property.SimpleIntegerProperty(tickets);
            this.revenue = new javafx.beans.property.SimpleDoubleProperty(revenue);
        }

        public String getSectionTitle() { return sectionTitle.get(); }
        public javafx.beans.property.StringProperty sectionTitleProperty() { return sectionTitle; }

        public int getTickets() { return tickets.get(); }
        public void setTickets(int value) { tickets.set(value); }
        public javafx.beans.property.IntegerProperty ticketsProperty() { return tickets; }

        public double getRevenue() { return revenue.get(); }
        public void setRevenue(double value) { revenue.set(value); }
        public javafx.beans.property.DoubleProperty revenueProperty() { return revenue; }
    }
}
