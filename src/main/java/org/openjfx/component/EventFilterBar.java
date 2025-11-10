package org.openjfx.component;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Event Filter Bar Component
 * Search and filter controls for events
 */
public class EventFilterBar extends HBox {
    
    private final TextField searchField;
    private final ComboBox<String> typeFilter;
    private final ComboBox<String> statusFilter;
    private final Button refreshBtn;
    
    public EventFilterBar() {
        super(15);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10, 0, 10, 0));
        
        searchField = new TextField();
        searchField.setPromptText("🔍 Search events...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");

        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "Football Match", "Concert", "Basketball", "Conference", "Other");
        typeFilter.setValue("All Types");
        typeFilter.setPrefWidth(180);
        typeFilter.setStyle("-fx-font-size: 14px;");

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "UPCOMING", "ONGOING", "COMPLETED", "CANCELLED");
        statusFilter.setValue("All Status");
        statusFilter.setPrefWidth(180);
        statusFilter.setStyle("-fx-font-size: 14px;");

        refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 15; -fx-cursor: hand;");
        FontAwesomeIconView refreshIcon = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        refreshIcon.setSize("14");
        refreshBtn.setGraphic(refreshIcon);
        
        getChildren().addAll(searchField, typeFilter, statusFilter, refreshBtn);
    }
    
    public TextField getSearchField() { return searchField; }
    public ComboBox<String> getTypeFilter() { return typeFilter; }
    public ComboBox<String> getStatusFilter() { return statusFilter; }
    public Button getRefreshBtn() { return refreshBtn; }
    
    public void reset() {
        searchField.clear();
        typeFilter.setValue("All Types");
        statusFilter.setValue("All Status");
    }
}
