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
javafx.scene.text.Font font = javafx.scene.text.Font.loadFont(
            de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView.class
            .getResourceAsStream("/de/jensd/fx/glyphs/fontawesome/fontawesome-webfont.ttf"), 
            10
         );

         // 2. Check if it is NULL
         if (font == null) {
            System.out.println("❌ FAILED: loadFont returned NULL. The path is wrong or module is blocked.");
         } else {
            System.out.println("✅ SUCCESS: Loaded font: " + font.getFamily());
            // 3. Print all families to verify it is registered
            System.out.println("Registered Families: " + javafx.scene.text.Font.getFamilies());
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