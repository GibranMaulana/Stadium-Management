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
        try {
         // This specific path points to the .ttf file inside the 'fontawesomefx-fontawesome' jar
         javafx.scene.text.Font.loadFont(
               de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView.class
                  .getResourceAsStream("/de/jensd/fx/glyphs/fontawesome/fontawesome-webfont.ttf"), 
               10
         );

         System.out.println("success loading font");
      } catch (Exception e) {
         System.out.println("WARNING: Could not load FontAwesome font!");
         e.printStackTrace();
      }
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