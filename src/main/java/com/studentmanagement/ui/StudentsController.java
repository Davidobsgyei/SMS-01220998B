package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import com.studentmanagement.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class StudentsController {

    // FXML Fields (Ensure these match your FXML fx:id names!)
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colId, colName, colProgramme, colStatus;
    @FXML private TableColumn<Student, Double> colGpa;
    @FXML private TextField idInput, nameInput, gpaInput, searchField;
    @FXML private Label totalCountLabel;
    @FXML private Label topPerformerLabel;
    @FXML private Label atRiskLabel;
    @FXML private PieChart gpaChart;
    @FXML private TabPane mainTabPane; // Matches fx:id in the new FXML
    @FXML private Label activeCountLabel;
    @FXML private Label inactiveCountLabel;
    @FXML private Label avgGpaLabel;
    @FXML private TextField excellentThresholdInput;
    @FXML private TextField atRiskThresholdInput;


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
    @FXML
    public void handleExportCSV() {
        File file = new File("students_report.csv");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("ID,Name,Programme,GPA,Status"); // Header
            for (Student s : studentList) {
                writer.printf("%s,%s,%s,%.2f,%s%n",
                        s.getStudentId(), s.getFullName(), s.getProgramme(), s.getGpa(), s.getStatus());
            }
            showAlert("Export Success", "Report saved to " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Export Failed", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    public void showTopPerformers() {
        try {
            List<Student> topStudents = studentService.getTopPerformers();
            studentTable.setItems(FXCollections.observableArrayList(topStudents));
            showAlert("Report Generated", "Showing students with GPA >= 3.5", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Report Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void showAtRiskStudents() {
        try {
            List<Student> atRisk = studentService.getAtRiskStudents();
            studentTable.setItems(FXCollections.observableArrayList(atRisk));
            showAlert("Report Generated", "Showing students with GPA < 2.0", Alert.AlertType.WARNING);
        } catch (Exception e) {
            showAlert("Report Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    public void loadStudentData() {
        try {
            // 1. Fetch the fresh list from the Service
            List<Student> allStudents = studentService.getAllStudents();

            // 2. Convert it to an ObservableList so the TableView can see it
            ObservableList<Student> data = FXCollections.observableArrayList(allStudents);

            // 3. Set the items in the table
            studentTable.setItems(data);

            System.out.println("Table successfully reset to show all students.");
        } catch (Exception e) {
            showAlert("Error", "Could not reload student data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void resetTable() {
        loadStudentData();
    }
    @FXML
    public void handleImportCSV() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Open Student CSV File");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        java.io.File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Call the service method we wrote earlier
            String report = studentService.importFromCSV(selectedFile);

            // Show the error report requirement
            showAlert("Import Report", report, Alert.AlertType.INFORMATION);

            // Refresh the table to show new students
            loadStudentData();
        }
    }




    @FXML
    public void updateDashboard() {
        try {
            // Always get fresh data from the service
            List<Student> students = studentService.getAllStudents();
            if (students.isEmpty()) return;

            // Requirement 9.1 Calculations
            long total = students.size();
            long active = students.stream().filter(s -> "Active".equalsIgnoreCase(s.getStatus())).count();
            long inactive = total - active;
            double avg = students.stream().mapToDouble(Student::getGpa).average().orElse(0.0);

            // Update Dashboard Labels
            totalCountLabel.setText(String.valueOf(total));
            activeCountLabel.setText(String.valueOf(active));
            inactiveCountLabel.setText(String.valueOf(inactive));
            avgGpaLabel.setText(String.format("%.2f", avg));

            // Update PieChart based on Settings thresholds
            long excellent = students.stream().filter(s -> s.getGpa() >= excellentThreshold).count();
            long risk = students.stream().filter(s -> s.getGpa() < atRiskThreshold).count();

            gpaChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Excellent", excellent),
                    new PieChart.Data("At Risk", risk),
                    new PieChart.Data("Normal", total - (excellent + risk))
            ));
        } catch (Exception e) {
            System.err.println("Dashboard error: " + e.getMessage());
        }
    }

    // Navigation Quick Buttons
    @FXML
    public void switchToStudentTab() {
        mainTabPane.getSelectionModel().select(0); // Selects the first tab
    }

    @FXML
    public void switchToSettingsTab() {
        mainTabPane.getSelectionModel().select(2); // Selects the third tab
    }
    @FXML
    public void handleSearch() {
        String query = searchField.getText().toLowerCase();

        if (query.isEmpty()) {
            loadStudentData(); // Reset to full list if search is empty
            return;
        }

        try {
            List<Student> allStudents = studentService.getAllStudents();
            List<Student> filteredList = allStudents.stream()
                    .filter(s -> s.getFullName().toLowerCase().contains(query) ||
                            s.getStudentId().toLowerCase().contains(query))
                    .collect(Collectors.toList());

            studentTable.setItems(FXCollections.observableArrayList(filteredList));
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
        }
    }

    private double excellentThreshold = 3.5;
    private double atRiskThreshold = 2.0;


    @FXML
    public void saveSettings() {
        try {
            // Update the actual variables used by the logic
            excellentThreshold = Double.parseDouble(excellentThresholdInput.getText());
            atRiskThreshold = Double.parseDouble(atRiskThresholdInput.getText());

            // REFRESH DATA: This is the missing link!
            updateDashboard();

            showAlert("Settings Saved", "Thresholds updated and Dashboard refreshed.", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for thresholds.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void navToManagement() {
        mainTabPane.getSelectionModel().select(1); // Moves to Student Management tab
    }

    @FXML
    public void navToSettings() {
        mainTabPane.getSelectionModel().select(2); // Moves to Settings tab
    }
}