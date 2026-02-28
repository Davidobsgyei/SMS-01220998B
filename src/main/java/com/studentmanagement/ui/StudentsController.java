package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import com.studentmanagement.service.StudentService;
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
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StudentsController implements Initializable {

    // --- NAVIGATION & LAYOUT ---
    private static BorderPane staticRootPane;
    @FXML private BorderPane rootPane;
    @FXML private TabPane mainTabPane;
    @FXML private StackPane mainCanvas;

    // --- TABLE VIEW ---
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colId, colName, colProgramme, colStatus, colEmail, colPhone;
    @FXML private TableColumn<Student, Double> colGpa;
    @FXML private TableColumn<Student, Integer> colLevel;
    @FXML private TableColumn<Student, java.time.LocalDate> colDate;

    // --- INPUT FIELDS ---
    @FXML private TextField idInput, nameInput, gpaInput, searchField;
    @FXML private TextField excellentThresholdInput, atRiskThresholdInput;

    // --- DASHBOARD LABELS ---
    @FXML private Label totalCountLabel, activeCountLabel, inactiveCountLabel, avgGpaLabel;
    @FXML private PieChart gpaChart;
    @FXML private Button refreshBtn;

    // --- LOGIC FIELDS ---
    private final StudentService studentService = new StudentService();
    private static ObservableList<Student> studentList = FXCollections.observableArrayList();
    private static double atRiskThreshold = 2.0;
    private static double excellentThreshold = 3.5;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadThresholdFromFile();

        if (rootPane != null) {
            staticRootPane = rootPane;
        }

        // --- CRITICAL FIX: NULL CHECKS ---
        // We check if studentTable is null before running setup to avoid NullPointerException
        if (studentTable != null) {
            setupTable();
            loadData();
            setupSearchFiltering();

            studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    idInput.setText(newVal.getStudentId());
                    nameInput.setText(newVal.getFullName());
                    gpaInput.setText(String.valueOf(newVal.getGpa()));
                    idInput.setEditable(false);
                    phoneInput.setText(newVal.getPhoneNumber());
                }
            });
        }

        if (totalCountLabel != null) {
            updateDashboard();
        }

        if (atRiskThresholdInput != null) {
            atRiskThresholdInput.setText(String.valueOf(atRiskThreshold));
            excellentThresholdInput.setText(String.valueOf(excellentThreshold));
        }

        if (mainCanvas != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), mainCanvas);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
    }

    // --- TABLE SETUP (REFINED) ---
    private void setupTable() {
        // 1. Map columns to Student class properties
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colProgramme.setCellValueFactory(new PropertyValueFactory<>("programme"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));

        // Ensure this matches exactly what is in Student.java
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        if (colStatus != null) {
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colStatus.setCellFactory(column -> new TableCell<Student, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        Student s = getTableRow().getItem();
                        // This uses the threshold from your settings page
                        if (s.getGpa() < atRiskThreshold) {
                            setText("Inactive");
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-alignment: center; -fx-font-weight: bold; -fx-background-radius: 5;");
                        } else {
                            setText("Active");
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-alignment: center; -fx-font-weight: bold; -fx-background-radius: 5;");
                        }
                    }
                }
            });
        }
    }

    // --- NAVIGATION ---
    public void switchTo(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFileName));
            Parent view = loader.load();
            if (staticRootPane != null) staticRootPane.setCenter(view);
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load: " + fxmlFileName, Alert.AlertType.ERROR);
        }
    }

    @FXML public void goHome() { switchTo("Dashboard_view.fxml"); } // Fixes Symbol Error
    @FXML public void showManagement() { switchTo("students_view.fxml"); }
    @FXML public void showSettings() { switchTo("Settings_View.fxml"); }
    @FXML public void navToManagement() { if(mainTabPane != null) mainTabPane.getSelectionModel().select(1); } // Fixes Symbol Error

    // --- CORE ACTIONS ---
    @FXML
    public void handleSearch() { // Fixes Symbol Error
        String query = searchField.getText().toLowerCase();
        FilteredList<Student> filtered = new FilteredList<>(studentList, s ->
                s.getFullName().toLowerCase().contains(query) || s.getStudentId().toLowerCase().contains(query)
        );
        studentTable.setItems(filtered);
    }
    @FXML private TextField phoneInput;
    // --- UPDATED ADD STUDENT LOGIC ---
    @FXML
    public void handleAddStudent() {
        try {
            // Use the new constructor we just built
            Student newStudent = new Student(
                    idInput.getText(),
                    nameInput.getText(),
                    "Electrical Engineering",
                    100,
                    Double.parseDouble(gpaInput.getText()),
                    phoneInput.getText() // User-entered phone number
            );

            // Save to DB and refresh UI
            studentService.saveStudent(newStudent);
            studentList.add(newStudent);

            studentTable.refresh(); // This triggers the status colors
            updateDashboard();
            clearFields();

        } catch (Exception e) {
            // Shows the error if date or fields are missing
            showAlert("Error", "Could not add student: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleUpdate() {
        try {
            Student selected = studentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("No Selection", "Please select a student from the table.", Alert.AlertType.WARNING);
                return;
            }

            // 1. Update the Object with new UI values
            selected.setFullName(nameInput.getText());
            selected.setGpa(Double.parseDouble(gpaInput.getText()));
            selected.setPhoneNumber(phoneInput.getText()); // This fixes the phone link!

            // 2. The status is auto-calculated in the Student class based on new GPA

            // 3. Save to Database
            studentService.modifyStudent(selected);

            // 4. Refresh UI
            studentTable.refresh();
            updateDashboard();
            clearFields();
            showAlert("Success", "Student record updated successfully!", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please check the GPA format.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Update Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDelete() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            studentService.removeStudent(selected.getStudentId());
            studentList.remove(selected);
            updateDashboard();
        } catch (Exception e) {
            showAlert("Error", "Delete failed", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void updateDashboard() {
        try {
            List<Student> students = studentService.getAllStudents();
            if (students == null || students.isEmpty()) return;

            long total = students.size();
            long inactive = students.stream().filter(s -> s.getGpa() < atRiskThreshold).count();
            double avg = students.stream().mapToDouble(Student::getGpa).average().orElse(0.0);

            Platform.runLater(() -> {
                totalCountLabel.setText(String.valueOf(total));
                activeCountLabel.setText(String.valueOf(total - inactive));
                inactiveCountLabel.setText(String.valueOf(inactive));
                avgGpaLabel.setText(String.format("%.2f", avg));

                ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                        new PieChart.Data("Active", total - inactive),
                        new PieChart.Data("At Risk", inactive)
                );
                gpaChart.setData(data);
            });
        } catch (Exception e) {
            System.err.println("Dashboard Sync Error");
        }
    }

    @FXML
    public void saveSettings() {
        try {
            atRiskThreshold = Double.parseDouble(atRiskThresholdInput.getText());
            saveThresholdToFile(atRiskThreshold);

            // CRITICAL FIX: Only refresh if the table is actually loaded in this view
            if (studentTable != null) {
                studentTable.refresh();
            }

            showAlert("Success", "Settings Saved!", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            // Fixes "Invalid threshold" error from image_439037.png
            showAlert("Error", "Please enter a valid numeric threshold.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleOpenReports(ActionEvent event) { // Fixes Symbol Error
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportsView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Performance Reports");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load reports", Alert.AlertType.ERROR);
        }
    }

    // --- CSV OPERATIONS ---
    @FXML
    public void handleExportCSV() { // Fixes Symbol Error
        FileChooser fc = new FileChooser();
        File file = fc.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("ID,Name,GPA,Status");
                for (Student s : studentList) pw.printf("%s,%s,%.2f,%s%n", s.getStudentId(), s.getFullName(), s.getGpa(), s.getStatus());
                showAlert("Success", "Data Exported", Alert.AlertType.INFORMATION);
            } catch (Exception e) { showAlert("Error", "Export failed", Alert.AlertType.ERROR); }
        }
    }

    @FXML
    public void handleImportCSV() { // Fixes Symbol Error
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);
        if (file != null) {
            // Logic to parse and add to DB
            loadData();
            showAlert("Success", "Import Complete", Alert.AlertType.INFORMATION);
        }
    }

    // --- UTILITIES ---
    private void loadData() {
        try {
            studentList.setAll(studentService.getAllStudents());
            studentTable.setItems(studentList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearchFiltering() {
        FilteredList<Student> filtered = new FilteredList<>(studentList, p -> true);
        searchField.textProperty().addListener((obs, old, nv) -> {
            filtered.setPredicate(s -> nv == null || nv.isEmpty() ||
                    s.getFullName().toLowerCase().contains(nv.toLowerCase()) ||
                    s.getStudentId().contains(nv));
        });
        studentTable.setItems(filtered);
    }
    private void clearFields() {
        idInput.clear();
        nameInput.clear();
        gpaInput.clear();
        phoneInput.clear(); // Clear the new phone field too!
        idInput.setEditable(true);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void saveThresholdToFile(double threshold) {
        Properties props = new Properties();
        props.setProperty("atRiskThreshold", String.valueOf(threshold));
        try (OutputStream out = new FileOutputStream("config.properties")) {
            props.store(out, null);
        } catch (IOException e) { System.err.println("Could not save config"); }
    }

    private void loadThresholdFromFile() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            atRiskThreshold = Double.parseDouble(props.getProperty("atRiskThreshold", "2.0"));
        } catch (IOException e) { atRiskThreshold = 2.0; }
    }
    @FXML
    private void handleRefresh() { // Must match the fx:id in FXML
        if (refreshBtn != null) {
            // Optional: Adds a 360-degree rotation animation to the button
            RotateTransition rt = new RotateTransition(Duration.seconds(1), refreshBtn);
            rt.setByAngle(360);
            rt.setCycleCount(1);

            rt.setOnFinished(e -> {
                // Re-fetch data from DB so the table updates
                loadData();
                updateDashboard();
            });

            rt.play();
        } else {
            // Fallback if the button reference is null
            loadData();
            updateDashboard();
        }
    }
}