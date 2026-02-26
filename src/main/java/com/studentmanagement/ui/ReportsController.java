package com.studentmanagement.ui;

import com.studentmanagement.domain.Student;
import com.studentmanagement.service.StudentService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.util.List;

public class ReportsController {
    @FXML private PieChart gpaDistributionChart;
    @FXML private BarChart<String, Number> levelBarChart;
    @FXML private Label lblTotalStudents, lblAvgGpa;

    private final StudentService service = new StudentService();

    @FXML
    public void initialize() {
        try {
            List<Student> students = service.getAllStudents();
            updateSummary(students);
            loadPieChart(students);
            loadBarChart(students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSummary(List<Student> students) {
        lblTotalStudents.setText(String.valueOf(students.size()));
        double avg = students.stream().mapToDouble(Student::getGpa).average().orElse(0.0);
        lblAvgGpa.setText(String.format("%.2f", avg));
    }

    private void loadPieChart(List<Student> students) {
        long excellent = students.stream().filter(s -> s.getGpa() >= 3.5).count();
        long atRisk = students.stream().filter(s -> s.getGpa() < 1.5).count();
        long average = students.size() - (excellent + atRisk);

        gpaDistributionChart.getData().add(new PieChart.Data("Excellent", excellent));
        gpaDistributionChart.getData().add(new PieChart.Data("Average", average));
        gpaDistributionChart.getData().add(new PieChart.Data("At Risk", atRisk));
    }

    private void loadBarChart(List<Student> students) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Students per Level");

        // Example: Counting Level 100 students
        long level100 = students.stream().filter(s -> s.getLevel() == 100).count();
        series.getData().add(new XYChart.Data<>("Level 100", level100));

        levelBarChart.getData().add(series);
    }
    @FXML
    private void closeReports(javafx.event.ActionEvent event) {
        // This gets the current window and closes it
        ((javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()).close();
    }
}