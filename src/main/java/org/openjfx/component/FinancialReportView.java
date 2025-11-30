package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.ScaleTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.application.Platform;
import javafx.util.Duration;
import org.openjfx.model.Event;
import org.openjfx.service.EventService;
import org.openjfx.service.ReportService;

import java.time.LocalDate;
import java.util.List;

/**
 * Financial report UI: shows revenue/expenses/profit for a date range and per-event summary
 */
public class FinancialReportView extends VBox {

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
        setSpacing(20);
        setPadding(new Insets(0));
        
        // Set default date range (last month to today)
        startDate.setValue(LocalDate.now().minusMonths(1));
        endDate.setValue(LocalDate.now());
        
        // Summary cards at top
        HBox summaryCards = createSummaryCards();
        
        // Controls card
        VBox controlsCard = new VBox(15);
        controlsCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        controlsCard.setPadding(new Insets(20));
        setupCardHoverAnimation(controlsCard);

        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().addAll(
            new Label("Period:"), 
            startDate, 
            new Label("to"), 
            endDate
        );

        Button apply = new Button("Apply Filter");
        apply.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
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
        btnShowAll.setOnAction(e -> { 
            showAll = !showAll; 
            btnShowAll.setText(showAll? "Show Latest 5" : "Show All"); 
            updateChart(); 
        });
        
        HBox pagination = new HBox(8);
        pagination.setAlignment(Pos.CENTER_LEFT);
        pagination.getChildren().addAll(btnPrev, pageLabel, btnNext, btnShowAll);
        
        controlsCard.getChildren().addAll(controls, pagination);

        // Bar chart setup
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Profit (Rp)");
        profitBarChart = new BarChart<>(xAxis, yAxis);
        profitBarChart.setTitle("Profit per Event");
        profitBarChart.setLegendVisible(false);
        profitBarChart.setAnimated(false);
        profitBarChart.setPrefHeight(400);
        
        VBox chartCard = new VBox(profitBarChart);
        chartCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        chartCard.setPadding(new Insets(20));
        VBox.setVgrow(chartCard, Priority.ALWAYS);

        getChildren().addAll(summaryCards, controlsCard, chartCard);

        refresh();
        // register for data-change notifications so charts refresh when expenses are added
        ReportService.addRefreshListener(() -> Platform.runLater(this::refresh));
    }
    
    private HBox createSummaryCards() {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        
        VBox revenueCard = createStatCard("Revenue", lblRevenue, FontAwesomeIcon.MONEY, "#27ae60");
        VBox expensesCard = createStatCard("Expenses", lblExpenses, FontAwesomeIcon.SHOPPING_CART, "#e74c3c");
        VBox profitCard = createStatCard("Profit", lblProfit, FontAwesomeIcon.LINE_CHART, "#3498db");
        
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        HBox.setHgrow(expensesCard, Priority.ALWAYS);
        HBox.setHgrow(profitCard, Priority.ALWAYS);
        
        container.getChildren().addAll(revenueCard, expensesCard, profitCard);
        return container;
    }
    
    private VBox createStatCard(String title, Label valueLabel, FontAwesomeIcon icon, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("24");
        iconView.setFill(javafx.scene.paint.Color.web(color));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        
        header.getChildren().addAll(iconView, titleLabel);
        
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label rpLabel = new Label("Rp");
        rpLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
        
        card.getChildren().addAll(header, valueLabel, rpLabel);
        
        // Add hover animation
        setupCardHoverAnimation(card);
        
        return card;
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
                
                // Filter by event date within period
                evs.removeIf(ev -> {
                    LocalDate eventDate = ev.getEventDate();
                    if (eventDate == null) return true; // exclude if no date
                    if (eventDate.isBefore(start)) return true;
                    if (eventDate.isAfter(end)) return true;
                    return false;
                });
                
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
                    double profit = reportService.getProfitForEvent(ev.getId());
                    series.getData().add(new XYChart.Data<>(ev.getEventName(), profit));
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
    
    private void setupCardHoverAnimation(javafx.scene.Node card) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        card.setOnMouseEntered(e -> scaleUp.play());
        card.setOnMouseExited(e -> scaleDown.play());
    }
}
