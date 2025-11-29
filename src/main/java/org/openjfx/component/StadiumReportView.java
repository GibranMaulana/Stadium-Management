package org.openjfx.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openjfx.model.Event;
import org.openjfx.model.EventSection;
import org.openjfx.service.EventService;
import org.openjfx.service.EventSectionService;
import org.openjfx.service.ReportService;

import java.util.List;
import java.util.Map;

/**
 * Stadium report UI: shows section popularity and event-level summary
 */
public class StadiumReportView extends BorderPane {

    private final ReportService reportService = new ReportService();
    private final EventService eventService = new EventService();
    private final EventSectionService eventSectionService = new EventSectionService();

    private final TableView<SectionRow> sectionsTable = new TableView<>();
    private final PieChart pieChart = new PieChart();
    private final ComboBox<Event> eventSelector = new ComboBox<>();

    public StadiumReportView() {
        setPadding(new Insets(12));

        VBox container = new VBox(10);
        container.setPadding(new Insets(8));

        Label title = new Label("Stadium Reports");

        // Event selector
        HBox selectorBar = new HBox(8);
        selectorBar.setPadding(new Insets(6,0,6,0));
        eventSelector.setPromptText("Select event");
        List<Event> events = eventService.getAllEvents();
        eventSelector.setItems(FXCollections.observableArrayList(events));
        eventSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEventName());
            }
        });
        eventSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEventName());
            }
        });

        Button loadBtn = new Button("Load");
        loadBtn.setOnAction(e -> loadEventStats());
        selectorBar.getChildren().addAll(new Label("Event:"), eventSelector, loadBtn);

        TableColumn<SectionRow, String> colSection = new TableColumn<>("Section");
        colSection.setCellValueFactory(data -> data.getValue().sectionProperty());
        colSection.setPrefWidth(300);

        TableColumn<SectionRow, Integer> colTickets = new TableColumn<>("Tickets Sold");
        colTickets.setCellValueFactory(data -> data.getValue().ticketsProperty().asObject());
        colTickets.setPrefWidth(140);

        sectionsTable.getColumns().add(colSection);
        sectionsTable.getColumns().add(colTickets);

        container.getChildren().addAll(title, selectorBar, pieChart, sectionsTable);

        setCenter(container);

        refresh();
    }

    private void refresh() {
        Map<String, Integer> popularity = reportService.getSectionPopularity();
        ObservableList<SectionRow> rows = FXCollections.observableArrayList();
        popularity.forEach((k, v) -> rows.add(new SectionRow(k, v)));
        sectionsTable.setItems(rows);
    }

    private void loadEventStats() {
        Event ev = eventSelector.getValue();
        if (ev == null) return;

        List<EventSection> sections = eventSectionService.getEventSections(ev.getId());

        int totalCapacity = 0;
        int totalBooked = 0;
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (EventSection s : sections) {
            totalCapacity += s.getTotalCapacity();
            totalBooked += s.getBookedSeats();
            pieData.add(new PieChart.Data(s.getSectionTitle(), s.getBookedSeats()));
        }

        int available = totalCapacity - totalBooked;
        pieData.add(new PieChart.Data("Available", Math.max(available, 0)));

        pieChart.setData(pieData);
        pieChart.setTitle(ev.getEventName() + " — Booked vs Available");

        // update sections table with booking numbers
        ObservableList<SectionRow> rows = FXCollections.observableArrayList();
        for (EventSection s : sections) rows.add(new SectionRow(s.getSectionTitle(), s.getBookedSeats()));
        sectionsTable.setItems(rows);
    }

    public static class SectionRow {
        private final javafx.beans.property.SimpleStringProperty section;
        private final javafx.beans.property.SimpleIntegerProperty tickets;

        public SectionRow(String section, int tickets) {
            this.section = new javafx.beans.property.SimpleStringProperty(section);
            this.tickets = new javafx.beans.property.SimpleIntegerProperty(tickets);
        }

        public String getSection() { return section.get(); }
        public javafx.beans.property.StringProperty sectionProperty() { return section; }

        public int getTickets() { return tickets.get(); }
        public javafx.beans.property.IntegerProperty ticketsProperty() { return tickets; }
    }
}
