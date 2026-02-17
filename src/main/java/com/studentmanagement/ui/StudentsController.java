package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import com.studentmanagement.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class StudentsController {

    // 1. Keep these - they link to your FXML
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, Double> colGpa;
    @FXML private TableColumn<Student,String> colStatus;
    @FXML private TableColumn<Student, String> colProgramme;
    @FXML private TextField programmeInput;
    @FXML private TextField idInput;
    @FXML private TextField nameInput;
    @FXML private TextField gpaInput;


    // 2. Add the Service Layer and the List
    private final StudentService studentService = new StudentService();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up how the table displays data
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colProgramme.setCellValueFactory(new PropertyValueFactory<>("programme"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Load existing data from the database
        loadData();
    }

    private void loadData() {
        try {
            studentList.clear();
            studentList.addAll(studentService.getAllStudents());
            studentTable.setItems(studentList);
        } catch (Exception e) {
            System.err.println("Load error: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddStudent() {
        try {
            // Create the student object from UI inputs
            Student newStudent = new Student(
                    idInput.getText(),
                    nameInput.getText(),
                    "ELECTRICAL/ELECTRONIC ENGINEERING", // Placeholder for now
                    100,
                    Double.parseDouble(gpaInput.getText()),
                    "test@test.com",
                    "0240000000",
                    "Active"
            );

            // SEND TO SERVICE for validation and saving
            studentService.saveStudent(newStudent);

            // Update UI only if the service didn't throw an error
            studentList.add(newStudent);
            clearFields();

            System.out.println("Success: Student saved to database!");

        } catch (NumberFormatException e) {
            System.err.println("Error: GPA must be a number!");
        } catch (Exception e) {
            // This catches your custom validation errors (e.g., "Full name cannot contain numbers")
            System.err.println("Validation Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        idInput.clear();
        nameInput.clear();
        gpaInput.clear();
    }
}