package org.openjfx.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Container view for Reports with three styled sub-buttons: Financial, Event, Stadium
 */
public class ReportsMainView extends BorderPane {

    private final Button btnFinancial = new Button("Financial");
    private final Button btnEvent = new Button("Event");
    private final Button btnStadium = new Button("Stadium");

    private final StackPane contentPane = new StackPane();

    public ReportsMainView() {
        setPadding(new Insets(16));

        // Title + description
        Label title = new Label("Reports");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Label desc = new Label("Provides strategic analysis through Financial Reports, Event Performance summaries, and an Overall Stadium Operations");
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #7f8c8d;");

        VBox header = new VBox(6, title, desc);
        header.setPadding(new Insets(0,0,12,0));

        // Button bar (styled like menu buttons)
        HBox buttonBar = new HBox(8);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        styleMenuButton(btnFinancial);
        styleMenuButton(btnEvent);
        styleMenuButton(btnStadium);

        btnFinancial.setOnAction(e -> showFinancial());
        btnEvent.setOnAction(e -> showEvent());
        btnStadium.setOnAction(e -> showStadium());

        buttonBar.getChildren().addAll(btnFinancial, btnEvent, btnStadium);

        // Initialize content views
        FinancialReportView financialView = new FinancialReportView();
        EventReportView eventView = new EventReportView();
        StadiumReportView stadiumView = new StadiumReportView();

        contentPane.getChildren().addAll(financialView, eventView, stadiumView);

        // hide all then show default view
        for (int i = 0; i < contentPane.getChildren().size(); i++) {
            contentPane.getChildren().get(i).setVisible(false);
            contentPane.getChildren().get(i).setManaged(false);
        }

        // Default view: Event
        showEvent();

        VBox container = new VBox(10, header, buttonBar, contentPane);
        container.setPadding(new Insets(8));

        setCenter(container);
    }

    private void styleMenuButton(Button b) {
        // apply inactive style by default
        applyInactiveStyle(b);
        b.setOnMouseEntered(e -> applyHoverStyle(b));
        b.setOnMouseExited(e -> {
            // if this button is active, keep active style; otherwise inactive
            if (isActiveButton(b)) applyActiveStyle(b); else applyInactiveStyle(b);
        });
    }

    private void applyActiveStyle(Button b) {
        b.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 13px; " +
                   "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
    }

    private void applyInactiveStyle(Button b) {
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #2c3e50; -fx-font-size: 13px; " +
                   "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
    }

    private void applyHoverStyle(Button b) {
        b.setStyle("-fx-background-color: #ecf3ff; -fx-text-fill: #2c3e50; -fx-font-size: 13px; " +
                   "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
    }

    private Button activeButton = null;

    private boolean isActiveButton(Button b) {
        return activeButton == b;
    }

    private void setActiveButton(Button b) {
        // reset all
        applyInactiveStyle(btnFinancial);
        applyInactiveStyle(btnEvent);
        applyInactiveStyle(btnStadium);
        // apply active
        applyActiveStyle(b);
        activeButton = b;
    }

    private void showFinancial() {
        setActiveButton(btnFinancial);
        setVisibleIndex(0);
    }

    private void showEvent() {
        setActiveButton(btnEvent);
        setVisibleIndex(1);
    }

    private void showStadium() {
        setActiveButton(btnStadium);
        setVisibleIndex(2);
    }

    private void setVisibleIndex(int index) {
        for (int i = 0; i < contentPane.getChildren().size(); i++) {
            boolean show = (i == index);
            contentPane.getChildren().get(i).setVisible(show);
            contentPane.getChildren().get(i).setManaged(show);
        }
    }
}
