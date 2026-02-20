package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.repository.DatabaseConnection;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
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
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;

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
    private  TableView<Student> studentTable;
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
    private static ObservableList<Student> studentList = FXCollections.observableArrayList();
    private static double excellentThreshold = 3.5;
    private static double atRiskThreshold = 2.0;
    // Add this line at the very top with your other @FXML variables
    private static TableView<Student> sharedTable;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Capture MainShell Root (The Bridge)
        if (rootPane != null) {
            staticRootPane = rootPane;
        }
        if (studentTable != null) {
            sharedTable = studentTable; // Link the UI table to our bridge
            setupTable();
            loadStudentData();
        }
        if (mainCanvas != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), mainCanvas);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
        // Fill the settings fields with the current standards so they aren't empty
        if (inactiveThresholdInput != null) {
            inactiveThresholdInput.setText(String.valueOf(inactiveThreshold));
            excellentThresholdInput.setText(String.valueOf(excellentThreshold));
            atRiskThresholdInput.setText(String.valueOf(atRiskThreshold));
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
        long inactiveCount = studentList.stream()
                .filter(s -> "Inactive".equalsIgnoreCase(s.getStatus()) || s.getGpa() < inactiveThreshold)
                .count();

        if (inactiveCountLabel != null) {
            inactiveCountLabel.setText(String.valueOf(inactiveCount));
        }

        // Update your PieChart to show the "Inactive" slice
        if (gpaChart != null) {
            // ... (Update chart data)
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
        if (totalCountLabel == null) return; // Prevent errors if dashboard isn't loaded

        long inactiveCount = studentList.stream()
                .filter(s -> "Inactive".equalsIgnoreCase(s.getStatus()))
                .count();

        totalCountLabel.setText(String.valueOf(total));
        inactiveCountLabel.setText(String.valueOf(inactiveCount));
        activeCountLabel.setText(String.valueOf(total - inactiveCount));

        // Refresh your PieChart data here too

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
            // 1. Capture new thresholds
            this.excellentThreshold = Double.parseDouble(excellentThresholdInput.getText());
            this.atRiskThreshold = Double.parseDouble(atRiskThresholdInput.getText());
            this.inactiveThreshold = Double.parseDouble(inactiveThresholdInput.getText());

            // 2. Loop through and Sync
            for (Student s : studentList) {
                String oldStatus = s.getStatus();
                String newStatus = (s.getGpa() < inactiveThreshold) ? "Inactive" : "Active";

                // Only trigger a database hit if the status actually changed
                if (!newStatus.equals(oldStatus)) {
                    s.setStatus(newStatus);
                    // PERSISTENCE: Save to DB
                    studentService.updateStudentStatus(s.getStudentId(), newStatus);
                }
            }

            // 3. Refresh UI
            sharedTable.refresh();
            updateDashboard();

            showAlert("Sync Complete", "Database updated with new academic standards.", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid threshold values.", Alert.AlertType.ERROR);
        }
        try {
            // 1. Update the static standards from the text fields
            excellentThreshold = Double.parseDouble(excellentThresholdInput.getText());
            atRiskThreshold = Double.parseDouble(atRiskThresholdInput.getText());
            inactiveThreshold = Double.parseDouble(inactiveThresholdInput.getText());

            // 2. Loop through the list and update the database
            for (Student s : studentList) {
                String newStatus = (s.getGpa() < inactiveThreshold) ? "Inactive" : "Active";

                if (!newStatus.equals(s.getStatus())) {
                    s.setStatus(newStatus);
                    // Sync with Database
                    studentService.updateStudentStatus(s.getStudentId(), newStatus);
                }
            }

            // 3. Force the table to repaint with new colors
            if (sharedTable != null) {
                sharedTable.refresh();
            }

            showAlert("Success", "Standards applied globally!", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers (e.g., 2.5)", Alert.AlertType.ERROR);
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
        try {
            List<Student> students = studentService.getAllStudents();

            // APPLY THE STANDARD:
            // Automatically mark students as Inactive if they fall below the threshold
            for (Student s : students) {
                if (s.getGpa() < inactiveThreshold) {
                    s.setStatus("Inactive");
                } else if (s.getStatus().equals("Inactive") && s.getGpa() >= inactiveThreshold) {
                    // Optional: reactivate if they improve
                    s.setStatus("Active");
                }
            }

            studentList.setAll(students);
            updateDashboardStats();
        } catch (Exception e) {
            e.printStackTrace();
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
    // Inside StudentsController.java

    private double inactiveThreshold = 1.0; // The standard for "Academic Inactivity"

    @FXML
    private TextField inactiveThresholdInput; // Add this to your Settings FXML later
    private void applyStatusStandard() {
        for (Student s : studentList) {
            // Apply the "Standard": If GPA is below threshold, they are Inactive
            if (s.getGpa() < inactiveThreshold) {
                s.setStatus("Inactive");
            } else {
                // Otherwise, they stay/become Active
                s.setStatus("Active");
            }

}
    }private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));

        // CUSTOM CELL FACTORY FOR STATUS COLORS
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    getStyleClass().removeAll("status-active", "status-inactive");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Inactive")) {
                        getStyleClass().add("status-inactive");
                        getStyleClass().remove("status-active");
                    } else {
                        getStyleClass().add("status-active");
                        getStyleClass().remove("status-inactive");
                    }
                }
            }
        });
        colStatus.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Apply different colors based on the status text
                    if (item.equalsIgnoreCase("Inactive")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red for Inactive
                    } else if (item.equalsIgnoreCase("Active")) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green for Active
                    } else {
                        setStyle("-fx-text-fill: #34495e;"); // Default dark blue/grey
                    }
                }
            }
        });

    }
    @FXML private StackPane mainCanvas; // Add fx:id="mainCanvas" to your FXML StackPane
    private void updateChart(int active, int inactive) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Active", active),
                new PieChart.Data("Inactive", inactive)
        );

        gpaChart.setData(pieChartData);

        // Apply specific colors to slices after they are rendered
        for (PieChart.Data data : gpaChart.getData()) {
            if (data.getName().equals("Active")) {
                data.getNode().getStyleClass().add("chart-active");
            } else {
                data.getNode().getStyleClass().add("chart-inactive");
            }
        }

    }
    @FXML private Button refreshBtn;

    @FXML
    private void handleRefresh() {
        // 1. Create the spin animation
        RotateTransition rt = new RotateTransition(Duration.seconds(1), refreshBtn);
        rt.setByAngle(360);
        rt.setCycleCount(1);

        // 2. Run the logic when animation starts
        rt.setOnFinished(e -> {
            updateDashboard(); // Refresh your stats and chart
            studentTable.refresh(); // Refresh the table
        });

        rt.play();
    }

               }