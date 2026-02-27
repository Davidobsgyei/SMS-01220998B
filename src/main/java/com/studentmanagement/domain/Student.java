package com.studentmanagement.domain;

import java.time.LocalDate;

public class Student {
    private String studentId;
    private String fullName;
    private String email;
    private int level;
    private double gpa;
    private String phoneNumber;
    private String programme;
    private LocalDate dateAdded;
    private String status;

    // 1. Default Constructor
    public Student() {
        this.dateAdded = LocalDate.now(); // Default to today to prevent null crashes
    }

    // 2. Full Constructor (Used by Repository when loading existing data)
    public Student(String studentId, String fullName, String email, int level,
                   double gpa, String phoneNumber, String programme, LocalDate dateAdded, String status) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.level = level;
        this.gpa = gpa;
        this.phoneNumber = phoneNumber;
        this.programme = programme;
        this.dateAdded = (dateAdded != null) ? dateAdded : LocalDate.now();
        this.status = status;
    }

    // 3. UI Constructor (Used by handleAddStudent in Controller)
    // Removed 'email' from parameters so it can be generated automatically
    public Student(String studentId, String fullName, String programme, int level,
                   double gpa, String phoneNumber) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.programme = programme;
        this.level = level;
        this.gpa = gpa;
        this.phoneNumber = phoneNumber;

        // AUTOMATIC LOGIC
        // 1. Generate Email: "first.last@school.edu"
        this.email = fullName.toLowerCase().trim().replace(" ", ".") + "@school.edu";

        // 2. Set Date: Fixes the LocalDate.toString() null error
        this.dateAdded = LocalDate.now();

        // 3. Set Status: Based on threshold
        this.status = (gpa < 2.0) ? "Inactive" : "Active";
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public double getGpa() { return gpa; }
    public void setGpa(double gpa) {
        this.gpa = gpa;
        this.status = (gpa < 2.0) ? "Inactive" : "Active"; // Auto-update status
    }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
    public LocalDate getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDate dateAdded) { this.dateAdded = dateAdded; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}