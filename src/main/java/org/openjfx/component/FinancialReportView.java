package org.openjfx.component;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openjfx.model.Event;
import org.openjfx.service.EventService;
import org.openjfx.service.ReportService;

import java.time.LocalDate;
import java.util.List;

/**
 * Financial report UI: shows revenue/expenses/profit for a date range and per-event summary
 */
public class FinancialReportView extends BorderPane {

    private final ReportService reportService = new ReportService();

    private final DatePicker startDate = new DatePicker();
    private final DatePicker endDate = new DatePicker();
    private final Label lblRevenue = new Label("0");
    private final Label lblExpenses = new Label("0");
    private final Label lblProfit = new Label("0");
    
    private final BarChart<String, Number> profitBarChart;
    private final EventService eventService = new EventService();
    private List<Event> allEvents;
    private int page = 0;
    private final int pageSize = 5;
    private final Button btnPrev = new Button("◀");
    private final Button btnNext = new Button("▶");
    private final Button btnShowAll = new Button("Show All");
    private final Label pageLabel = new Label("");
    private boolean showAll = false;

    public FinancialReportView() {
        setPadding(new Insets(12));

        HBox controls = new HBox(8);
        controls.setPadding(new Insets(8));
        controls.getChildren().addAll(new Label("From:"), startDate, new Label("To:"), endDate);

        Button apply = new Button("Apply");
        apply.setOnAction(e -> refresh());
        controls.getChildren().add(apply);

        // paging controls for last-5 sliding window
        btnPrev.setOnAction(e -> {
            if (page > 0) page--; updateChart();
        });
        btnNext.setOnAction(e -> {
            int maxPage = Math.max(0, (allEvents == null ? 0 : (allEvents.size()-1)/pageSize));
            if (page < maxPage) page++; updateChart();
        });
        btnShowAll.setOnAction(e -> { showAll = !showAll; btnShowAll.setText(showAll? "Show Latest 5" : "Show All"); updateChart(); });
        controls.getChildren().addAll(btnPrev, pageLabel, btnNext, btnShowAll);

        VBox top = new VBox(6, controls);

        GridPane summary = new GridPane();
        summary.setHgap(12);
        summary.setVgap(8);
        summary.add(new Label("Revenue (Rp):"), 0, 0);
        summary.add(lblRevenue, 1, 0);
        summary.add(new Label("Expenses (Rp):"), 0, 1);
        summary.add(lblExpenses, 1, 1);
        summary.add(new Label("Profit (Rp):"), 0, 2);
        summary.add(lblProfit, 1, 2);


        // Bar chart setup
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Profit %");
        profitBarChart = new BarChart<>(xAxis, yAxis);
        profitBarChart.setTitle("Profit Percentage per Event");
        profitBarChart.setLegendVisible(false);
        profitBarChart.setAnimated(false);
        profitBarChart.setPrefHeight(320);

        setTop(top);

        HBox centerBox = new HBox(12);
        centerBox.setPadding(new Insets(8));
        HBox.setHgrow(profitBarChart, javafx.scene.layout.Priority.ALWAYS);
        profitBarChart.setMaxWidth(Double.MAX_VALUE);
        centerBox.getChildren().addAll(profitBarChart, summary);
        setCenter(centerBox);

        refresh();
    }

    private void refresh() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().minusMonths(1);
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();

        double revenue = reportService.getRevenueForPeriod(start, end);
        double expenses = reportService.getTotalExpenses(start, end);
        double profit = reportService.getProfitForPeriod(start, end);

        lblRevenue.setText(String.format("%,.2f", revenue));
        lblExpenses.setText(String.format("%,.2f", expenses));
        lblProfit.setText(String.format("%,.2f", profit));

        // load events list once and then update chart view
        Task<List<Event>> loadTask = new Task<>() {
            @Override
            protected List<Event> call() throws Exception {
                List<Event> evs = eventService.getAllEvents();
                // sort by date descending
                evs.sort((a,b) -> {
                    if (a.getEventDate() == null && b.getEventDate() == null) return 0;
                    if (a.getEventDate() == null) return 1;
                    if (b.getEventDate() == null) return -1;
                    return b.getEventDate().compareTo(a.getEventDate());
                });
                return evs;
            }
        };

        loadTask.setOnSucceeded(ev -> {
            allEvents = loadTask.getValue();
            page = 0; // show latest
            updateChart();
        });
        new Thread(loadTask).start();
    }

    private void updateChart() {
        if (allEvents == null) return;
        Task<XYChart.Series<String, Number>> task = new Task<>() {
            @Override
            protected XYChart.Series<String, Number> call() throws Exception {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                List<Event> evs;
                if (showAll) evs = allEvents;
                else {
                    int start = page * pageSize;
                    int end = Math.min(start + pageSize, allEvents.size());
                    evs = allEvents.subList(start, end);
                }
                for (Event ev : evs) {
                    double revenue = reportService.getRevenueForEvent(ev.getId());
                    double profit = reportService.getProfitForEvent(ev.getId());
                    double pct = 0.0;
                    if (revenue != 0) pct = (profit / revenue) * 100.0;
                    series.getData().add(new XYChart.Data<>(ev.getEventName(), pct));
                }
                return series;
            }
        };

        task.setOnSucceeded(evt -> {
            profitBarChart.getData().clear();
            profitBarChart.getData().add(task.getValue());
            // update page label and controls
            int maxPage = Math.max(0, (allEvents.size() - 1) / pageSize);
            pageLabel.setText(showAll ? "All events" : String.format("Page %d/%d", page+1, maxPage+1));
            btnNext.setDisable(page <= 0 || showAll);
            btnPrev.setDisable((page >= maxPage) || showAll);
        });

        new Thread(task).start();
    }

    public static class EventRow {
        private final javafx.beans.property.SimpleStringProperty eventName;
        private final javafx.beans.property.SimpleDoubleProperty revenue;

        public EventRow(String eventName, double revenue) {
            this.eventName = new javafx.beans.property.SimpleStringProperty(eventName);
            this.revenue = new javafx.beans.property.SimpleDoubleProperty(revenue);
        }

        public String getEventName() { return eventName.get(); }
        public javafx.beans.property.StringProperty eventNameProperty() { return eventName; }

        public double getRevenue() { return revenue.get(); }
        public javafx.beans.property.DoubleProperty revenueProperty() { return revenue; }
    }
}
