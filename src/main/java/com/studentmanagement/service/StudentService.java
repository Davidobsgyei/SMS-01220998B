package com.studentmanagement.service;

import com.studentmanagement.domain.Student;
import com.studentmanagement.repository.StudentRepository;
import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;


public class StudentService {
    private final StudentRepository repository = new StudentRepository();
    private double inactiveThreshold = 1.5;

    public String importFromCSV(File file) {
        int successCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(",");
                    if (data.length < 8) continue;

                    Student s = new Student();
                    s.setStudentId(data[0].trim());
                    s.setFullName(data[1].trim());
                    s.setEmail(data[2].trim());
                    s.setLevel(Integer.parseInt(data[3].trim())); // Parse int
                    s.setGpa(Double.parseDouble(data[4].trim())); // Parse double
                    s.setPhoneNumber(data[5].trim());
                    s.setProgramme(data[6].trim());
                    s.setDateAdded(LocalDate.parse(data[7].trim())); // Parse Date
                    s.setStatus(s.getGpa() < inactiveThreshold ? "Inactive" : "Active");

                    repository.addStudent(s);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Skipping bad row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            return "File Error: " + e.getMessage();
        }
        return "Imported " + successCount + " students.";
    }

    public List<Student> getAllStudents() throws Exception { return repository.getAllStudents(); }
    public void saveStudent(Student s) throws Exception { repository.addStudent(s); }
    public void removeStudent(String id) throws Exception { repository.deleteStudent(id); }
    public void modifyStudent(Student s) throws Exception { repository.updateStudent(s); }











    public List<Student> getTopPerformers() throws Exception {
        return repository.getAllStudents().stream()
                .filter(s -> s.getGpa() >= 3.5)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Student> getAtRiskStudents() throws Exception {
        return repository.getAllStudents().stream()
                .filter(s -> s.getGpa() < 2.0)
                .collect(java.util.stream.Collectors.toList());
    }
    public double getInactiveThreshold() {
        return 1.5; // Or your preferred threshold
    }
    public void updateStudentStatus(String studentId, String newStatus) {
        try {
            // Wrap the database call to handle the SQLException
            repository.updateStudentStatus(studentId, newStatus);
        } catch (SQLException e) {
            // Log the error instead of letting the app crash
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}