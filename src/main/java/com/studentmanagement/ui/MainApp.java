package com.studentmanagement.ui;

import com.studentmanagement.repository.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database on startup
        DatabaseConnection.initializeDatabase();

        // For now, we create a simple placeholder to test if JavaFX runs
        Label label = new Label("Student Management System Plus - Skeleton Loaded");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Student Management System Plus");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}