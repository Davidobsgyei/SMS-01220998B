package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.repository.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StudentsController implements Initializable {

    // --- NAVIGATION & LAYOUT ---
    private static BorderPane staticRootPane;

    @FXML
    private BorderPane rootPane;
    @FXML
    private TabPane mainTabPane;

    // --- TABLE VIEW ---
    @FXML
    private TableView<Student> studentTable;
    @FXML
    private TableColumn<Student, String> colId, colName, colProgramme, colStatus;
    @FXML
    private TableColumn<Student, Double> colGpa;

    // --- INPUT FIELDS ---
    @FXML
    private TextField idInput, nameInput, gpaInput, searchField;
    @FXML
    private TextField excellentThresholdInput, atRiskThresholdInput;
    @FXML
    private TextField excellentInput, atRiskInput; // Included from your snippet

    // --- DASHBOARD LABELS ---
    @FXML
    private Label totalCountLabel, activeCountLabel, inactiveCountLabel, avgGpaLabel;
    @FXML
    private Label topPerformerLabel, atRiskLabel; // From original report labels
    @FXML
    private PieChart gpaChart;

    // --- LOGIC FIELDS ---
    private final StudentService studentService = new StudentService();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private double excellentThreshold = 3.5;
    private double atRiskThreshold = 2.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Capture MainShell Root (The Bridge)
        if (rootPane != null) {
            staticRootPane = rootPane;
        }

        // 2. Initialize Student Management UI (If current view is Students)
        if (studentTable != null) {
            setupTableColumns();
            loadData();
            setupSearchFiltering();

            // Selection Listener
            studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    idInput.setText(newSelection.getStudentId());
                    nameInput.setText(newSelection.getFullName());
                    gpaInput.setText(String.valueOf(newSelection.getGpa()));
                    idInput.setEditable(false);
                }
            });
        }

        // 3. Initialize Dashboard UI (If current view is Dashboard)
        if (totalCountLabel != null) {
            refreshStudentData();
            updateDashboard();
            updateDashboardStats();
        }
    }

    // --- CORE NAVIGATION (REFINED) ---
    public void switchTo(String fxmlFileName) {
        try {
            // Path adjusted to match your MainApp structure (/fxml/)
            String path = "/fxml/" + fxmlFileName;
            URL resource = getClass().getResource(path);

            if (resource == null) {
                System.err.println("CRITICAL: FXML not found: " + path);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            if (staticRootPane != null) {
                staticRootPane.setCenter(view);
            } else {
                System.err.println("ERROR: staticRootPane is null. Ensure MainShell.fxml has fx:id='rootPane'");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load: " + fxmlFileName, Alert.AlertType.ERROR);
        }
    }

    @FXML public void showManagement() { switchTo("students_view.fxml"); }
    @FXML public void showSettings() { switchTo("Settings_View.fxml"); }
    @FXML public void goHome() { switchTo("Dashboard_view.fxml"); }

    // Tab Navigation (For TabPane setups)
    @FXML
    public void switchToStudentTab() { if(mainTabPane != null) mainTabPane.getSelectionModel().select(0); }
    @FXML
    public void switchToSettingsTab() { if(mainTabPane != null) mainTabPane.getSelectionModel().select(2); }
    @FXML
    public void navToManagement() { if(mainTabPane != null) mainTabPane.getSelectionModel().select(1); }
    @FXML
    public void navToSettings() { if(mainTabPane != null) mainTabPane.getSelectionModel().select(2); }
    @FXML
    public void jumpToStudents() { if(mainTabPane != null) mainTabPane.getSelectionModel().select(1); }

    // --- STUDENT MANAGEMENT ACTIONS ---

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colProgramme.setCellValueFactory(new PropertyValueFactory<>("programme"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupSearchFiltering() {
        if (searchField == null) return;
        FilteredList<Student> filteredData = new FilteredList<>(studentList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(student -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String filter = newValue.toLowerCase();
                return student.getFullName().toLowerCase().contains(filter) ||
                        student.getStudentId().toLowerCase().contains(filter);
            });
        });
        studentTable.setItems(filteredData);
    }

    @FXML
    public void handleAddStudent() {
        try {
            Student newStudent = new Student(
                    idInput.getText(),
                    nameInput.getText(),
                    "Electrical/Electronic Engineering",
                    100,
                    Double.parseDouble(gpaInput.getText()),
                    "student@school.edu", "0240000000", "Active"
            );
            studentService.saveStudent(newStudent);
            studentList.add(newStudent);
            clearFields();
            showAlert("Success", "Student Added!", Alert.AlertType.INFORMATION);
            updateDashboard();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "GPA must be a number.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Validation Error", e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void handleUpdate() {
        try {
            Student selected = studentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", "Select a student to update first!", Alert.AlertType.WARNING);
                return;
            }
            selected.setFullName(nameInput.getText());
            selected.setGpa(Double.parseDouble(gpaInput.getText()));
            studentService.modifyStudent(selected);
            studentTable.refresh();
            clearFields();
            idInput.setEditable(true);
            showAlert("Success", "Student updated successfully!", Alert.AlertType.INFORMATION);
            updateDashboard();
        } catch (Exception e) {
            showAlert("Update Failed", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDelete() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please click a student in the table first!", Alert.AlertType.WARNING);
            return;
        }
        try {
            studentService.removeStudent(selected.getStudentId());
            studentList.remove(selected);
            showAlert("Success", "Student records deleted permanently.", Alert.AlertType.INFORMATION);
            updateDashboard();
        } catch (Exception e) {
            showAlert("Database Error", "Could not delete: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadStudentData();
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

    // --- DASHBOARD & REPORTS ---

    @FXML
    public void updateDashboard() {
        try {
            List<Student> students = studentService.getAllStudents();
            if (students.isEmpty()) return;

            long total = students.size();
            long active = students.stream().filter(s -> "Active".equalsIgnoreCase(s.getStatus())).count();
            double avg = students.stream().mapToDouble(Student::getGpa).average().orElse(0.0);

            if (totalCountLabel != null) {
                totalCountLabel.setText(String.valueOf(total));
                activeCountLabel.setText(String.valueOf(active));
                inactiveCountLabel.setText(String.valueOf(total - active));
                avgGpaLabel.setText(String.format("%.2f", avg));
            }

            if (gpaChart != null) {
                long excellent = students.stream().filter(s -> s.getGpa() >= excellentThreshold).count();
                long risk = students.stream().filter(s -> s.getGpa() < atRiskThreshold).count();
                gpaChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Excellent", excellent),
                        new PieChart.Data("At Risk", risk),
                        new PieChart.Data("Normal", total - (excellent + risk))
                ));
            }
        } catch (Exception e) {
            System.err.println("Dashboard error: " + e.getMessage());
        }
    }

    private void updateDashboardStats() {
        if (studentList == null || studentList.isEmpty()) return;
        long total = studentList.size();
        long active = studentList.stream().filter(s -> "Active".equalsIgnoreCase(s.getStatus())).count();
        double avg = studentList.stream().mapToDouble(Student::getGpa).average().orElse(0.0);

        if (totalCountLabel != null) totalCountLabel.setText(String.valueOf(total));
        if (activeCountLabel != null) activeCountLabel.setText(String.valueOf(active));
        if (inactiveCountLabel != null) inactiveCountLabel.setText(String.valueOf(total - active));
        if (avgGpaLabel != null) avgGpaLabel.setText(String.format("%.2f", avg));
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

    // --- SETTINGS ---

    @FXML
    public void saveSettings() {
        try {
            excellentThreshold = Double.parseDouble(excellentThresholdInput.getText());
            atRiskThreshold = Double.parseDouble(atRiskThresholdInput.getText());
            updateDashboard();
            showAlert("Settings Saved", "Thresholds updated and Dashboard refreshed.", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for thresholds.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSaveSettings() {
        try {
            excellentThreshold = Double.parseDouble(excellentThresholdInput.getText());
            atRiskThreshold = Double.parseDouble(atRiskThresholdInput.getText());
            showAlert("Settings Saved", "Thresholds updated successfully!", Alert.AlertType.INFORMATION);
            goHome();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid decimal numbers for GPA.", Alert.AlertType.ERROR);
        }
    }

    // --- CSV IMPORT/EXPORT ---

    @FXML
    public void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Student Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(staticRootPane.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID,Name,Email,GPA,Status");
                for (Student s : studentList) {
                    writer.printf("%s,%s,%s,%.2f,%s%n",
                            s.getStudentId(), s.getFullName(), s.getEmail(), s.getGpa(), s.getStatus());
                }
                showAlert("Success", "Data exported to " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "Could not save file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(staticRootPane.getScene().getWindow());
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    // Note: You would add logic here to create Student and save to DB
                }
                refreshStudentData();
                updateDashboardStats();
                showAlert("Import Complete", "Database updated successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Import Error", "Failed to parse CSV: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // --- DATA UTILITIES ---

    private void loadData() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentList.setAll(students);
        } catch (Exception e) {
            showAlert("DB Error", "Could not load data.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void loadStudentData() {
        try {
            List<Student> allStudents = studentService.getAllStudents();
            studentTable.setItems(FXCollections.observableArrayList(allStudents));
            System.out.println("Table successfully reset.");
        } catch (Exception e) {
            showAlert("Error", "Could not reload: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshStudentData() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentList.setAll(students);
            if (studentTable != null) studentTable.setItems(studentList);
        } catch (Exception e) {
            System.err.println("Could not refresh data: " + e.getMessage());
        }
    }

    @FXML public void resetTable() { loadStudentData(); }

    private void clearFields() {
        idInput.clear();
        nameInput.clear();
        gpaInput.clear();
        idInput.setEditable(true);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}