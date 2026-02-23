package com.studentmanagement.service;

import com.studentmanagement.domain.Student;
import com.studentmanagement.repository.DatabaseConnection;
import com.studentmanagement.repository.StudentRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class StudentService {
    private final StudentRepository repository = new StudentRepository();

    // Default value - this now controls the "At Risk" logic globally
    private double inactiveThreshold = 1.5;

    public void saveStudent(Student student) throws Exception {
        if (student.getStudentId() == null || student.getStudentId().length() < 4) {
            throw new Exception("Student ID must be at least 4 characters long.");
        }
        if (student.getFullName().matches(".*\\d.*")) {
            throw new Exception("Full name cannot contain numbers.");
        }
        if (student.getGpa() < 0.0 || student.getGpa() > 4.0) {
            throw new Exception("GPA must be between 0.0 and 4.0.");
        }
        repository.addStudent(student);
    }

    public List<Student> getAllStudents() throws SQLException {
        return repository.getAllStudents();
    }

    public void removeStudent(String studentId) throws Exception {
        repository.deleteStudent(studentId);
    }

    // UPDATED: Now uses the dynamic threshold
    public List<Student> getTopPerformers() throws Exception {
        return repository.getAllStudents().stream()
                .filter(s -> s.getGpa() >= 3.5)
                .collect(Collectors.toList());
    }



    public void modifyStudent(Student student) throws Exception {
        if (student.getGpa() < 0 || student.getGpa() > 4.0) {
            throw new Exception("Invalid GPA. Must be between 0.0 and 4.0");
        }
        repository.updateStudent(student);
    }

    // This handles the "silent" add for your UI buttons
    public boolean addStudent(Student student) {
        try {
            saveStudent(student);
            return true;
        } catch (Exception e) {
            logError(e.getMessage());
            return false;
        }
    }

    public String importFromCSV(File file) {
        int successCount = 0;
        StringBuilder errorLog = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(",");
                    // Note: Ensure your Student constructor matches these 8 fields
                    Student s = new Student(data[0], data[1], data[2], 100, Double.parseDouble(data[3]), "email", "024", data[4]);
                    saveStudent(s);
                    successCount++;
                } catch (Exception e) {
                    errorLog.append("Failed row: ").append(line).append(" - ").append(e.getMessage()).append("\n");
                }
            }
        } catch (IOException e) {
            return "Critical Error: " + e.getMessage();
        }
        return "Successfully imported " + successCount + " students.";
    }

    public void exportToCSV(List<Student> students, java.io.File file) throws Exception {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            writer.println("Student ID,Full Name,Programme,GPA,Status");
            for (Student s : students) {
                writer.printf("%s,%s,%s,%.2f,%s%n",
                        s.getStudentId(), s.getFullName(), s.getProgramme(), s.getGpa(), s.getStatus());
            }
        }
    }

    private void logError(String message) {
        try (java.io.FileWriter fw = new java.io.FileWriter("error_log.txt", true);
             java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {
            pw.println(java.time.LocalDateTime.now() + " - ERROR: " + message);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void updateStudentStatus(String studentId, String newStatus) {
        String sql = "UPDATE students SET status = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Database Sync Error: " + e.getMessage());
        }
    }

    // THRESHOLD LOGIC
    public double getInactiveThreshold() {
        return this.inactiveThreshold;
    }

    public void setInactiveThreshold(double newThreshold) {
        this.inactiveThreshold = newThreshold;
    }
    // In StudentService.java
    private static final double INACTIVE_THRESHOLD = 1.5;

    public List<Student> getAtRiskStudents() throws Exception {
        return repository.getAllStudents().stream()
                .filter(s -> s.getGpa() < INACTIVE_THRESHOLD)
                .collect(Collectors.toList());
    }
}