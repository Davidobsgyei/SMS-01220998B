package com.studentmanagement.ui;

import com.studentmanagement.repository.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * The Main Entry point for the Student Management System Plus.
 * This class loads the MainShell, which acts as the persistent window frame.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialize the Database Connection first
            DatabaseConnection.initializeDatabase();

            // 2. Load the MainShell (The window with the BorderPane/rootPane)
            // Path matches the 'switchTo' logic in your Controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainShell.fxml"));
            Parent root = loader.load();

            // 3. Configure the Primary Scene
            // We use 1000x700 to ensure the TableView and Dashboard have enough room
            Scene scene = new Scene(root, 1000, 700);

            // Optional: Add a CSS stylesheet if you have one
            // scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            primaryStage.setTitle("Student Management System Plus");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not load MainShell.fxml");
            System.err.println("Ensure your FXML files are in: src/main/resources/fxml/");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Application failed to start.");
            e.printStackTrace();
        }
        // Try this exact block in your Start method or where you set the Scene

    }

    public static void main(String[] args) {
        // Launches the JavaFX Application
        launch(args);
    }
}