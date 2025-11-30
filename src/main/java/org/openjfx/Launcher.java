package org.openjfx;

/**
 * Launcher class for JavaFX application
 * This is required when packaging with jpackage to avoid
 * "JavaFX runtime components are missing" error
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
