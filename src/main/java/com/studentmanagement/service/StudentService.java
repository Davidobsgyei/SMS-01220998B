package com.studentmanagement.service;

import com.studentmanagement.domain.Student;
import com.studentmanagement.repository.StudentRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class StudentService {
    private final StudentRepository repository = new StudentRepository();



    public void saveStudent(Student student) throws Exception {
        // VALIDATION RULE 1: Student ID length (4-20 chars)
        if (student.getStudentId() == null || student.getStudentId().length() < 4) {
            throw new Exception("Student ID must be at least 4 characters long.");
        }

        // VALIDATION RULE 2: Full name must not contain digits
        if (student.getFullName().matches(".*\\d.*")) {
            throw new Exception("Full name cannot contain numbers.");
        }

        // VALIDATION RULE 3: GPA range (0.0 - 4.0)
        if (student.getGpa() < 0.0 || student.getGpa() > 4.0) {
            throw new Exception("GPA must be between 0.0 and 4.0.");
        }

        // If all pass, send to repository to save in SQLite
        repository.addStudent(student);
    }

    public List<Student> getAllStudents() throws SQLException {
        return repository.getAllStudents();
    }
    public void removeStudent(String studentId) throws Exception {
        // This calls the deleteStudent method we just added to the Repository
        repository.deleteStudent(studentId);
    }
    public List<Student> getTopPerformers() throws Exception {
        return repository.getAllStudents().stream()
                .filter(s -> s.getGpa() >= 3.5)
                .collect(Collectors.toList());
    }

    public List<Student> getAtRiskStudents() throws Exception {
        return repository.getAllStudents().stream()
                .filter(s -> s.getGpa() < 2.0)
                .collect(Collectors.toList());
    }


    public void modifyStudent(Student student) throws Exception {
        // Basic validation before updating
        if (student.getGpa() < 0 || student.getGpa() > 4.0) {
            throw new Exception("Invalid GPA. Must be between 0.0 and 4.0");
        }
        repository.updateStudent(student);
    }
    public String importFromCSV(File file) {
        int successCount = 0;
        StringBuilder errorLog = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(",");
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
        return "Successfully imported " + successCount + " students.\n\nErrors:\n" + (errorLog.length() == 0 ? "None" : errorLog.toString());
    }
    public void exportToCSV(List<Student> students, java.io.File file) throws Exception {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // 1. Write the Header Row
            writer.println("Student ID,Full Name,Programme,GPA,Status");

            // 2. Loop through students and write data
            for (Student s : students) {
                writer.printf("%s,%s,%s,%.2f,%s%n",
                        s.getStudentId(),
                        s.getFullName(),
                        s.getProgramme(),
                        s.getGpa(),
                        s.getStatus());
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
}