package org.openjfx.component;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.openjfx.model.Seat;

/**
 * Individual seat button component with visual states
 * States: AVAILABLE (green), BOOKED (red), SELECTED (blue)
 */
public class SeatButton extends Button {
    
    private final Seat seat;
    private SeatState state;
    
    public enum SeatState {
        AVAILABLE,
        BOOKED,
        SELECTED
    }
    
    public SeatButton(Seat seat) {
        this.seat = seat;
        this.state = seat.isAvailable() ? SeatState.AVAILABLE : SeatState.BOOKED;
        
        initializeUI();
        setupStyles();
        setupTooltip();
        setupClickHandler();
    }
    
    private void initializeUI() {
        // Display seat number only
        setText(String.valueOf(seat.getSeatNumber()));
        setMinSize(35, 35);
        setMaxSize(35, 35);
        setPrefSize(35, 35);
        setAlignment(Pos.CENTER);
        
        // Disable if already booked
        if (state == SeatState.BOOKED) {
            setDisable(true);
        }
    }
    
    private void setupStyles() {
        // Base styles
        setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;"
        );
        
        updateVisualState();
    }
    
    private void setupTooltip() {
        String tooltipText = seat.getSeatLabel();
        if (state == SeatState.BOOKED) {
            tooltipText += " (Booked)";
        }
        setTooltip(new Tooltip(tooltipText));
    }
    
    private void setupClickHandler() {
        setOnAction(e -> {
            if (state == SeatState.AVAILABLE) {
                setState(SeatState.SELECTED);
            } else if (state == SeatState.SELECTED) {
                setState(SeatState.AVAILABLE);
            }
        });
    }
    
    public void setState(SeatState newState) {
        this.state = newState;
        updateVisualState();
    }
    
    private void updateVisualState() {
        String baseStyle = getStyle();
        
        switch (state) {
            case AVAILABLE:
                setStyle(baseStyle +
                    "-fx-background-color: #4CAF50;" +
                    "-fx-text-fill: white;" +
                    "-fx-border-color: #45a049;" +
                    "-fx-border-width: 1px;"
                );
                break;
                
            case BOOKED:
                setStyle(baseStyle +
                    "-fx-background-color: #f44336;" +
                    "-fx-text-fill: white;" +
                    "-fx-opacity: 0.6;"
                );
                break;
                
            case SELECTED:
                setStyle(baseStyle +
                    "-fx-background-color: #2196F3;" +
                    "-fx-text-fill: white;" +
                    "-fx-border-color: #1976D2;" +
                    "-fx-border-width: 2px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.5), 5, 0, 0, 0);"
                );
                break;
        }
    }
    
    public Seat getSeat() {
        return seat;
    }
    
    public SeatState getState() {
        return state;
    }
    
    public boolean isSelected() {
        return state == SeatState.SELECTED;
    }
}
