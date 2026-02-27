package com.studentmanagement.repository;

import com.studentmanagement.domain.Student;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {
    public void addStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (student_id, full_name, email, level, gpa, phone_number, programme, date_added, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getFullName());
            ps.setString(3, s.getEmail());
            ps.setInt(4, s.getLevel());
            ps.setDouble(5, s.getGpa());
            ps.setString(6, s.getPhoneNumber());
            ps.setString(7, s.getProgramme());
            ps.setString(8, s.getDateAdded().toString());
            ps.setString(9, s.getStatus());
            ps.executeUpdate();
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getString("student_id"),
                        rs.getString("full_name"),
                        rs.getString("programme"),
                        rs.getInt("level"),
                        rs.getDouble("gpa"),
                        rs.getString("phone_number") // Pulling the phone number from DB
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
            pstmt.executeUpdate();
        }
    }

    public void updateStudent(Student s) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, gpa = ?, programme = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getFullName());
            pstmt.setDouble(2, s.getGpa());
            pstmt.setString(3, s.getProgramme());
            pstmt.setString(4, s.getStudentId());
            pstmt.executeUpdate();
        }
    }
    public void updateStudentStatus(String studentId, String newStatus) throws SQLException {
        String sql = "UPDATE students SET status = ? WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameters for the query
            pstmt.setString(1, newStatus);
            pstmt.setString(2, studentId);

            // Execute the update
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating status failed, no student found with ID: " + studentId);
            }
        }
    }
    public void saveStudent(Student student) throws SQLException {
        // Ensure "phone_number" is included in the SQL string
        String sql = "INSERT INTO students (student_id, full_name, email, level, gpa, phone_number, programme, date_added, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getFullName());
            pstmt.setString(3, student.getEmail());
            pstmt.setInt(4, student.getLevel());
            pstmt.setDouble(5, student.getGpa());
            pstmt.setString(6, student.getPhoneNumber()); // This links the phone input to the DB
            pstmt.setString(7, student.getProgramme());
            pstmt.setDate(8, java.sql.Date.valueOf(student.getDateAdded()));
            pstmt.setString(9, student.getStatus());

            pstmt.executeUpdate();
        }
    }

}