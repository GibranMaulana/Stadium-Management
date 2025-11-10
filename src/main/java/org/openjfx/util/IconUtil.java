package org.openjfx.util;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.paint.Color;

/**
 * Utility class for creating FontAwesome icons
 */
public class IconUtil {
    
    /**
     * Create a FontAwesome icon with default size (16)
     */
    public static FontAwesomeIconView createIcon(FontAwesomeIcon icon) {
        return createIcon(icon, 16);
    }
    
    /**
     * Create a FontAwesome icon with custom size
     */
    public static FontAwesomeIconView createIcon(FontAwesomeIcon icon, int size) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize(String.valueOf(size));
        return iconView;
    }
    
    /**
     * Create a FontAwesome icon with custom size and color
     */
    public static FontAwesomeIconView createIcon(FontAwesomeIcon icon, int size, String color) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize(String.valueOf(size));
        iconView.setStyle("-fx-fill: " + color + ";");
        return iconView;
    }
    
    /**
     * Create a white icon for dark backgrounds
     */
    public static FontAwesomeIconView createWhiteIcon(FontAwesomeIcon icon, int size) {
        return createIcon(icon, size, "white");
    }
    
    /**
     * Create a colored icon
     */
    public static FontAwesomeIconView createColoredIcon(FontAwesomeIcon icon, int size, Color color) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize(String.valueOf(size));
        iconView.setFill(color);
        return iconView;
    }
}
