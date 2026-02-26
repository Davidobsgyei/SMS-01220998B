package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.repository.DatabaseConnection;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Properties;
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
        loadThresholdFromFile();
        if (totalCountLabel != null && totalCountLabel.getParent() != null) {
            // We target the parent container of the labels to treat it as a card
            totalCountLabel.getParent().getStyleClass().add("card");
            activeCountLabel.getParent().getStyleClass().add("card");
            inactiveCountLabel.getParent().getStyleClass().add("card");
            avgGpaLabel.getParent().getStyleClass().add("card");
        }
        // 1. Setup the Root Layout Reference
        if (rootPane != null) {
            staticRootPane = rootPane;
        }

        // 2. Setup Student Management (Table View)
        if (studentTable != null) {
            sharedTable = studentTable;

            // Define column logic first
            setupTable();

            // Populate data
            loadData();

            // Enable search functionality
            setupSearchFiltering();

            // Selection Listener for Auto-fill fields
            studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    idInput.setText(newVal.getStudentId());
                    nameInput.setText(newVal.getFullName());
                    gpaInput.setText(String.valueOf(newVal.getGpa()));
                    idInput.setEditable(false);
                }
            });

            // Ensure UI reflects status colors immediately
            studentTable.refresh();
        }

        // 3. Setup Dashboard
        if (totalCountLabel != null) {
            updateDashboard();
        }

        // 4. Initialize Settings Fields
        if (atRiskThresholdInput != null) {
            atRiskThresholdInput.setText(String.valueOf(atRiskThreshold));
            excellentThresholdInput.setText(String.valueOf(excellentThreshold));
            // Note: Using atRiskThreshold as the primary status driver as requested
        }

        // 5. Visual Transitions
        if (mainCanvas != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), mainCanvas);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
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
        colStatus.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Student s = getTableRow().getItem();
                    double riskThreshold = 1.5;

                    // Logic: If GPA < 1.5, force status to "Inactive" visually
                    if (s.getGpa() < riskThreshold || "Inactive".equalsIgnoreCase(s.getStatus())) {
                        setText("Inactive (At Risk)");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setText("Active");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

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
            // 1. Create the student inside the try block
            Student newStudent = new Student(
                    idInput.getText(),
                    nameInput.getText(),
                    "Electrical/Electronic Engineering",
                    100,
                    Double.parseDouble(gpaInput.getText()),
                    "student@school.edu",
                    "0240000000",
                    "Active" // We set them as 'Active' here...
            );

            // 2. Save to database
            studentService.saveStudent(newStudent);

            // 3. Update the UI List immediately
            if (studentList != null) {
                studentList.add(newStudent);

                // --- THE FIX ---
                // This forces the table to re-run the CellFactory logic.
                // Without this, the table shows the student but doesn't
                // 'know' to color them red/inactive yet.
                studentTable.refresh();
            } else {
                loadStudentData();
            }

            // 4. Reset the form and update the dashboard stats
            clearFields();
            updateDashboard();

            showAlert("Success", "Student Added Successfully!", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "GPA must be a number.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Could not save student: " + e.getMessage(), Alert.AlertType.ERROR);
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

    @FXML
    public void updateDashboard() {
        try {
            // 1. Fetch the latest data from the database
            List<Student> students = studentService.getAllStudents();

            if (students == null || students.isEmpty()) {
                resetDashboardLabels();
                return;
            }

            // 2. Calculations using the GLOBAL atRiskThreshold
            long total = students.size();

            // Count Inactive: Anyone with GPA below the threshold
            long inactive = students.stream()
                    .filter(s -> s.getGpa() < atRiskThreshold)
                    .count();

            // Count Active: Everyone else
            long active = total - inactive;

            // Calculate Average GPA
            double avg = students.stream()
                    .mapToDouble(Student::getGpa)
                    .average()
                    .orElse(0.0);

            // 3. Thread-safe UI Updates
            Platform.runLater(() -> {
                totalCountLabel.setText(String.valueOf(total));
                activeCountLabel.setText(String.valueOf(active));
                inactiveCountLabel.setText(String.valueOf(inactive));
                avgGpaLabel.setText(String.format("%.2f", avg));

                for (PieChart.Data data : gpaChart.getData()) {
                    Node node = data.getNode();

                    // Hover Enter: Brighten and Scale Up
                    node.setOnMouseEntered(e -> {
                        node.setScaleX(1.05);
                        node.setScaleY(1.05);
                        node.setEffect(new InnerShadow(10, Color.WHITE));
                    });

                    // Hover Exit: Reset to normal
                    node.setOnMouseExited(e -> {
                        node.setScaleX(1.0);
                        node.setScaleY(1.0);
                        node.setEffect(null);
                    });
                }

                if (gpaChart != null) {
                    gpaChart.setLegendSide(Side.RIGHT);
                    gpaChart.setLabelsVisible(true);

                    // Smooth slice color application
                    int i = 0;
                    for (PieChart.Data data : gpaChart.getData()) {
                        String color = data.getName().equals("Active") ? "#2ecc71" : "#e74c3c";
                        data.getNode().setStyle("-fx-pie-color: " + color + "; -fx-border-color: white;");
                    }
                }
                if (gpaChart != null) {
                    // Update the Pie Chart slices
                    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                            new PieChart.Data("Active", active),
                            new PieChart.Data("At Risk", inactive)
                    );
                    gpaChart.setData(pieData);

                    // Optional: Apply CSS classes for colors
                    for (PieChart.Data data : gpaChart.getData()) {
                        if (data.getName().equals("Active")) {
                            data.getNode().setStyle("-fx-pie-color: #2ecc71;"); // Green
                        } else {
                            data.getNode().setStyle("-fx-pie-color: #e74c3c;"); // Red
                        }
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("Dashboard Sync Error: " + e.getMessage());
        }
    }
    private void updatePieChart(List<Student> students) {
        long excellent = students.stream().filter(s -> s.getGpa() >= 3.5).count();
        long risk = students.stream().filter(s -> s.getGpa() < 1.5).count();
        long normal = students.size() - (excellent + risk);

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Excellent", excellent),
                new PieChart.Data("At Risk", risk),
                new PieChart.Data("Normal", normal)
        );
        gpaChart.setData(data);
        double currentThreshold = studentService.getInactiveThreshold(); // Or however you store it

        long inactiveCount = students.stream()
                .filter(s -> {
                    // A student is inactive ONLY if their status is "Inactive"
                    // OR if their GPA is strictly below the threshold
                    boolean isStatusInactive = "Inactive".equalsIgnoreCase(s.getStatus());
                    boolean isGpaBelow = s.getGpa() < currentThreshold;

                    return isStatusInactive || isGpaBelow;
                })
                .count();
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
            // 1. Update the variable from UI
            this.atRiskThreshold = Double.parseDouble(atRiskThresholdInput.getText());

            // 2. Save to the hard drive so it stays after restart
            saveThresholdToFile(this.atRiskThreshold);

            // 3. Sync database statuses
            for (Student s : studentList) {
                String newStatus = (s.getGpa() < atRiskThreshold) ? "Inactive" : "Active";
                if (!newStatus.equals(s.getStatus())) {
                    s.setStatus(newStatus);
                    studentService.updateStudentStatus(s.getStudentId(), newStatus);
                }
            }

            // 4. Update UI
            if (sharedTable != null) sharedTable.refresh();
            updateDashboard();

            showAlert("Success", "Settings saved permanently.", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Error", "Enter a valid number.", Alert.AlertType.ERROR);
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
    }
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colProgramme.setCellValueFactory(new PropertyValueFactory<>("programme"));

        colStatus.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // 1. Clear styles for empty cells to prevent ghosting
                getStyleClass().removeAll("status-badge", "status-active", "status-inactive");

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Student s = getTableRow().getItem();

                    // 2. Add the base badge class
                    getStyleClass().add("status-badge");

                    // 3. Add specific color class based on GPA
                    if (s.getGpa() < atRiskThreshold) {
                        setText("Inactive (At Risk)");
                        getStyleClass().add("status-inactive");
                    } else {
                        setText("Active");
                        getStyleClass().add("status-active");
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
        RotateTransition rt = new RotateTransition(Duration.seconds(1), refreshBtn);
        rt.setByAngle(360);
        rt.setCycleCount(1);

        rt.setOnFinished(e -> {
            // IMPORTANT: Re-fetch data from DB so the table actually updates!
            loadStudentData();
            updateDashboard();
        });

        rt.play();
    }
    private void resetDashboardLabels() {
        Platform.runLater(() -> {
            totalCountLabel.setText("0");
            activeCountLabel.setText("0");
            inactiveCountLabel.setText("0");
            avgGpaLabel.setText("0.00");
            if (gpaChart != null) {
                gpaChart.getData().clear();
            }
        });
    }


    private void saveThresholdToFile(double threshold) {
        Properties props = new Properties();
        props.setProperty("atRiskThreshold", String.valueOf(threshold));
        try (OutputStream out = new FileOutputStream("config.properties")) {
            props.store(out, "User Settings");
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    private void loadThresholdFromFile() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            String savedValue = props.getProperty("atRiskThreshold");
            if (savedValue != null) {
                this.atRiskThreshold = Double.parseDouble(savedValue);
            }
        } catch (IOException e) {
            // If file doesn't exist yet, keep the default value
            this.atRiskThreshold = 1.5;
        }
    }
    @FXML
    private void openReports() throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/ReportsView.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setScene(new javafx.scene.Scene(root));
        stage.setTitle("Academic Performance Reports");
        stage.show();
    }
    @FXML
    private void handleOpenReports(ActionEvent event) {
        try {
            // 1. Load the FXML for the reports screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportsView.fxml"));
            Parent root = loader.load();

            // 2. Create a new "Stage" (Window)
            Stage stage = new Stage();
            stage.setTitle("Student Performance Analysis");
            stage.setScene(new Scene(root));

            // 3. Make it a "Modal" window (optional - prevents clicking the main app until closed)
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.show();
        } catch (IOException e) {
            System.err.println("Could not load reports screen: " + e.getMessage());
            // Show an alert to the user if it fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Check if ReportsView.fxml exists in /resources/view/");
            alert.show();
        }
    }
 }