package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class StudentsController {

    // Links to the FXID in your FXML
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colProgramme;
    @FXML private TableColumn<Student, Double> colGpa;
    @FXML private TableColumn<Student, String> colStatus;

    @FXML private TextField idInput;
    @FXML private TextField nameInput;
    @FXML private TextField programmeInput;
    @FXML private TextField gpaInput;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // This tells the table which field from the Student class goes in which column
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colProgramme.setCellValueFactory(new PropertyValueFactory<>("programme"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Step B: Tell the table to watch our list
        studentTable.setItems(studentList);
    }

    @FXML
    public void handleAddStudent() {
        // For Week 1, we just print to console to prove the UI works
        String name = nameInput.getText();
        String id = idInput.getText();
        System.out.println("UI Test: Attempting to add student: " + name + " (ID: " + id + ")");

        // Clear fields after clicking
        nameInput.clear();
        idInput.clear();
        String prog = programmeInput.getText();
        double gpa = Double.parseDouble(gpaInput.getText()); // Warning: Simple version for now

        // Create a new student object
        Student newStudent = new Student(id, name, prog, 100, gpa, "email@test.com", "0240000000", "Active");

        // Add to the list that the table is watching
        studentList.add(newStudent);

        System.out.println("UI Test: Added " + name + " to the visible table.");

        // Clear the inputs
        idInput.clear();
        nameInput.clear();
    }
}