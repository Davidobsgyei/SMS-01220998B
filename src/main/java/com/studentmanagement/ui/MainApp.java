package com.studentmanagement.ui;

import com.studentmanagement.repository.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseConnection.initializeDatabase(); // [cite: 188]

        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/students_view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Student Management System Plus");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}