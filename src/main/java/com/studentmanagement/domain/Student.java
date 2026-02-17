package com.studentmanagement.domain;

import java.time.LocalDate;

public class Student {
    // Private fields: Data is hidden from outside the class [cite: 22]
    private String studentId;
    private String fullName;
    private String programme;
    private int level;
    private double gpa;
    private String email;
    private String phoneNumber;
    private LocalDate dateAdded;
    private String status;

    // Constructor to initialize the object [cite: 22]
    public Student(String studentId, String fullName, String programme, int level,
                   double gpa, String email, String phoneNumber, String status) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.programme = programme;
        this.level = level;
        this.gpa = gpa;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateAdded = LocalDate.now(); // Automatically set current date [cite: 58]
        this.status = status;
    }

    // Public Getters and Setters: Controlled access to the fields [cite: 22]

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDate dateAdded) { this.dateAdded = dateAdded; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}