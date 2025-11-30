package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
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
public class StadiumReportView extends VBox {

    private final ReportService reportService = new ReportService();
    private final EventService eventService = new EventService();
    private final EventSectionService eventSectionService = new EventSectionService();

    private final TableView<SectionRow> sectionsTable = new TableView<>();
    private final PieChart pieChart = new PieChart();
    private final ComboBox<Event> eventSelector = new ComboBox<>();

    public StadiumReportView() {
        setSpacing(20);
        setPadding(new Insets(0));

        // Event selector card
        VBox selectorCard = new VBox(15);
        selectorCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        selectorCard.setPadding(new Insets(20));
        setupCardHoverAnimation(selectorCard);

        Label selectorLabel = new Label("Select Event");
        selectorLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        selectorLabel.setStyle("-fx-text-fill: #2c3e50;");

        HBox selectorBar = new HBox(12);
        selectorBar.setAlignment(Pos.CENTER_LEFT);
        
        eventSelector.setPromptText("Select event");
        eventSelector.setPrefWidth(300);
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

        Button loadBtn = new Button("Load Report");
        FontAwesomeIconView loadIcon = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        loadIcon.setSize("12");
        loadIcon.setFill(javafx.scene.paint.Color.WHITE);
        loadBtn.setGraphic(loadIcon);
        loadBtn.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        loadBtn.setOnAction(e -> loadEventStats());
        
        selectorBar.getChildren().addAll(new Label("Event:"), eventSelector, loadBtn);
        selectorCard.getChildren().addAll(selectorLabel, selectorBar);

        // Chart card
        VBox chartCard = new VBox(15);
        chartCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        chartCard.setPadding(new Insets(20));
        setupCardHoverAnimation(chartCard);

        Label chartLabel = new Label("Section Occupancy");
        chartLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        chartLabel.setStyle("-fx-text-fill: #2c3e50;");

        pieChart.setPrefHeight(300);
        pieChart.setLegendVisible(true);
        
        chartCard.getChildren().addAll(chartLabel, pieChart);

        // Table card
        VBox tableCard = new VBox(15);
        tableCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        tableCard.setPadding(new Insets(20));
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        setupCardHoverAnimation(tableCard);

        Label tableLabel = new Label("Section Details");
        tableLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        tableLabel.setStyle("-fx-text-fill: #2c3e50;");

        TableColumn<SectionRow, String> colSection = new TableColumn<>("Section");
        colSection.setCellValueFactory(data -> data.getValue().sectionProperty());
        colSection.setPrefWidth(300);

        TableColumn<SectionRow, Integer> colTickets = new TableColumn<>("Tickets Sold");
        colTickets.setCellValueFactory(data -> data.getValue().ticketsProperty().asObject());
        colTickets.setPrefWidth(140);

        sectionsTable.getColumns().add(colSection);
        sectionsTable.getColumns().add(colTickets);
        VBox.setVgrow(sectionsTable, Priority.ALWAYS);

        tableCard.getChildren().addAll(tableLabel, sectionsTable);

        getChildren().addAll(selectorCard, chartCard, tableCard);

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
            
            // Hanya tampilkan section yang ada bookingnya (ticket sold > 0)
            if (s.getBookedSeats() > 0) {
                pieData.add(new PieChart.Data(s.getSectionTitle(), s.getBookedSeats()));
            }
        }

        int available = totalCapacity - totalBooked;
        // Hanya tampilkan Available jika memang ada kursi tersedia
        if (available > 0) {
            pieData.add(new PieChart.Data("Available", available));
        }

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
    
    private void setupCardHoverAnimation(javafx.scene.Node card) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
        scaleUp.setToX(1.01);
        scaleUp.setToY(1.01);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        card.setOnMouseEntered(e -> scaleUp.play());
        card.setOnMouseExited(e -> scaleDown.play());
    }
}
