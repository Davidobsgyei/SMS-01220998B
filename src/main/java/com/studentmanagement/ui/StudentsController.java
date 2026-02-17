package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import com.studentmanagement.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class StudentsController {

    // FXML Fields (Ensure these match your FXML fx:id names!)
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colId, colName, colProgramme, colStatus;
    @FXML private TableColumn<Student, Double> colGpa;
    @FXML private TextField idInput, nameInput, gpaInput, searchField;

    private final StudentService studentService = new StudentService();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Link Columns to Student class properties
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colProgramme.setCellValueFactory(new PropertyValueFactory<>("programme"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // This listens for mouse clicks on the table
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                idInput.setText(newSelection.getStudentId());
                nameInput.setText(newSelection.getFullName());
                gpaInput.setText(String.valueOf(newSelection.getGpa()));
                // Prevent editing the ID since it's the Primary Key
                idInput.setEditable(false);
            }
        });

        // 2. Load Data from Database
        loadData();

        // 3. Setup Search Filtering
        FilteredList<Student> filteredData = new FilteredList<>(studentList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(student -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String filter = newValue.toLowerCase();
                return student.getFullName().toLowerCase().contains(filter) ||
                        student.getStudentId().toLowerCase().contains(filter);
            });
        });

        // 4. Bind Table to Filtered List
        studentTable.setItems(filteredData);
    }

    @FXML
    public void handleAddStudent() {
        try {
            // Create Student Object
            Student newStudent = new Student(
                    idInput.getText(),
                    nameInput.getText(),
                    "Electrical/Electronic Engineering",
                    100,
                    Double.parseDouble(gpaInput.getText()),
                    "student@school.edu", "0240000000", "Active"
            );

            // Save via Service (This triggers validation)
            studentService.saveStudent(newStudent);

            // Update UI
            studentList.add(newStudent);
            clearFields();

            showAlert("Success", "Student Added!", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "GPA must be a number.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Validation Error", e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void handleDelete() {
        // 1. Identify who is selected in the TableView
        Student selected = studentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Please click a student in the table first!", Alert.AlertType.WARNING);
            return;
        }

        // 2. Ask for confirmation (Optional but professional)
        try {
            // 3. Delete from Database via the Service
            studentService.removeStudent(selected.getStudentId());

            // 4. Remove from the UI list (The table will update automatically)
            studentList.remove(selected);

            showAlert("Success", "Student records deleted permanently.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Database Error", "Could not delete: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadData() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentList.setAll(students);
        } catch (Exception e) {
            showAlert("DB Error", "Could not load data.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        idInput.clear();
        nameInput.clear();
        gpaInput.clear();
    }
    @FXML
    public void handleUpdate() {
        try {
            Student selected = studentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", "Select a student to update first!", Alert.AlertType.WARNING);
                return;
            }

            // Update the object with new values from text fields
            selected.setFullName(nameInput.getText());
            selected.setGpa(Double.parseDouble(gpaInput.getText()));

            // Save to DB
            studentService.modifyStudent(selected);

            // Refresh the table UI
            studentTable.refresh();
            clearFields();
            idInput.setEditable(true);

            showAlert("Success", "Student updated successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Update Failed", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}