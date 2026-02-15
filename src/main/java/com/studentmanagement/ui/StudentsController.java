package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class StudentsController {

    @FXML private TableView<Student> studentTable;
    @FXML private TextField nameField;
    @FXML private TextField idField;

    @FXML
    public void handleAddStudent() {
        // This will be implemented in Week 2 with service layer calls
        System.out.println("Add button clicked");
    }
}