package com.studentmanagement.repository;

import com.studentmanagement.domain.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    // Requirement: Add a new student [cite: 41]
    public void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_id, full_name, programme, level, gpa, email, phone_number, date_added, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Using Prepared Statements for all updates [cite: 93]
            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getFullName());
            pstmt.setString(3, student.getProgramme());
            pstmt.setInt(4, student.getLevel());
            pstmt.setDouble(5, student.getGpa());
            pstmt.setString(6, student.getEmail());
            pstmt.setString(7, student.getPhoneNumber());
            pstmt.setString(8, student.getDateAdded().toString());
            pstmt.setString(9, student.getStatus());

            pstmt.executeUpdate();
        }
    }

    // Requirement: View all students in a table [cite: 42]
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Mapping database rows to Student domain objects [cite: 98]
                Student s = new Student(
                        rs.getString("student_id"),
                        rs.getString("full_name"),
                        rs.getString("programme"),
                        rs.getInt("level"),
                        rs.getDouble("gpa"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("status")
                );
                students.add(s);
            }
        }
        return students;
    }
    public void deleteStudent(String studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Deleting student failed, no rows affected.");
            }
        }
    }
    public void updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, gpa = ?, programme = ? WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getFullName());
            pstmt.setDouble(2, student.getGpa());
            pstmt.setString(3, student.getProgramme());
            pstmt.setString(4, student.getStudentId());

            pstmt.executeUpdate();
        }
    }
    // Option A: Just pass the error up (easiest)
    public void removeStudent(String studentId) throws Exception {
        deleteStudent(studentId);
    }
}