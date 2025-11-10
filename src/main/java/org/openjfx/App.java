package org.openjfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.openjfx.controller.LoginController;


/**
 * Stadium Management System - Main Application
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Set up the primary stage
        stage.setTitle("Stadium Management System - Login");
        stage.setResizable(false);
        
        // Create and show login scene
        LoginController loginController = new LoginController(stage);
        Scene loginScene = loginController.getScene();
        
        stage.setScene(loginScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}