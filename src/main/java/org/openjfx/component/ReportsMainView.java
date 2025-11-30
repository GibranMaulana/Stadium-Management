package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Container view for Reports with three styled sub-buttons: Financial, Event, Stadium
 */
public class ReportsMainView extends VBox {

    private final Button btnFinancial = new Button("Financial Report");
    private final Button btnEvent = new Button("Event Report");
    private final Button btnStadium = new Button("Stadium Report");

    private final StackPane contentPane = new StackPane();
    private Button activeButton = null;

    public ReportsMainView() {
        setSpacing(0);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #ecf0f1;");

        // Header with icon
        VBox header = createHeader();

        // Button bar
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setPadding(new Insets(15, 0, 15, 0));

        styleMenuButton(btnFinancial);
        styleMenuButton(btnEvent);
        styleMenuButton(btnStadium);

        btnFinancial.setOnAction(e -> showFinancial());
        btnEvent.setOnAction(e -> showEvent());
        btnStadium.setOnAction(e -> showStadium());

        buttonBar.getChildren().addAll(btnFinancial, btnEvent, btnStadium);

        // Content area
        contentPane.setPadding(new Insets(20, 0, 0, 0));
        VBox.setVgrow(contentPane, Priority.ALWAYS);

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

        // Default view: Financial
        showFinancial();

        getChildren().addAll(header, buttonBar, contentPane);
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.BAR_CHART);
        icon.setSize("32");
        icon.setFill(javafx.scene.paint.Color.web("#2c3e50"));
        
        VBox textBox = new VBox(5);
        Label titleLabel = new Label("Reports & Analytics");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("View financial and operational reports");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        textBox.getChildren().addAll(titleLabel, subtitleLabel);
        titleBox.getChildren().addAll(icon, textBox);
        
        headerBox.getChildren().add(titleBox);
        return headerBox;
    }

    private void styleMenuButton(Button b) {
        // apply inactive style by default
        applyInactiveStyle(b);
        b.setOnMouseEntered(e -> {
            if (!isActiveButton(b)) {
                applyHoverStyle(b);
            }
        });
        b.setOnMouseExited(e -> {
            // if this button is active, keep active style; otherwise inactive
            if (isActiveButton(b)) {
                applyActiveStyle(b);
            } else {
                applyInactiveStyle(b);
            }
        });
    }

    private void applyActiveStyle(Button b) {
        b.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
    }

    private void applyInactiveStyle(Button b) {
        b.setStyle(
            "-fx-background-color: white; " +
            "-fx-text-fill: #2c3e50; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5; " +
            "-fx-border-color: #bdc3c7; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        );
    }

    private void applyHoverStyle(Button b) {
        b.setStyle(
            "-fx-background-color: #ecf0f1; " +
            "-fx-text-fill: #2c3e50; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5; " +
            "-fx-border-color: #3498db; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        );
    }

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
        setVisibleIndexWithAnimation(0);
    }

    private void showEvent() {
        setActiveButton(btnEvent);
        setVisibleIndexWithAnimation(1);
    }

    private void showStadium() {
        setActiveButton(btnStadium);
        setVisibleIndexWithAnimation(2);
    }

    private void setVisibleIndex(int index) {
        for (int i = 0; i < contentPane.getChildren().size(); i++) {
            boolean show = (i == index);
            contentPane.getChildren().get(i).setVisible(show);
            contentPane.getChildren().get(i).setManaged(show);
        }
    }
    
    private void setVisibleIndexWithAnimation(int index) {
        // Hide all views first
        for (int i = 0; i < contentPane.getChildren().size(); i++) {
            if (i != index) {
                contentPane.getChildren().get(i).setVisible(false);
                contentPane.getChildren().get(i).setManaged(false);
            }
        }
        
        // Show and animate the selected view
        javafx.scene.Node targetView = contentPane.getChildren().get(index);
        targetView.setVisible(true);
        targetView.setManaged(true);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), targetView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        // Slide in animation from left
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), targetView);
        slideIn.setFromX(30);
        slideIn.setToX(0);
        
        // Play both animations together
        ParallelTransition transition = new ParallelTransition(fadeIn, slideIn);
        transition.play();
    }
}
