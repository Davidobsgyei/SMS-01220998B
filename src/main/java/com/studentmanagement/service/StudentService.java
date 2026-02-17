package com.studentmanagement.service;

import com.studentmanagement.domain.Student;
import com.studentmanagement.repository.StudentRepository;
import java.sql.SQLException;
import java.util.List;

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
    public void modifyStudent(Student student) throws Exception {
        // Basic validation before updating
        if (student.getGpa() < 0 || student.getGpa() > 4.0) {
            throw new Exception("Invalid GPA. Must be between 0.0 and 4.0");
        }
        repository.updateStudent(student);
    }
}