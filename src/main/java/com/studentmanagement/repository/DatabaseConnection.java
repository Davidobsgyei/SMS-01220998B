package com.studentmanagement.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // The database must be saved inside the project 'data' folder [cite: 72]
    private static final String URL = "jdbc:sqlite:data/students.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        // SQL matching your strict requirements [cite: 80, 83, 84, 90, 92]
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS students (
                student_id TEXT PRIMARY KEY NOT NULL,
                full_name TEXT NOT NULL,
                programme TEXT NOT NULL,
                level INTEGER NOT NULL CHECK (level IN (100, 200, 300, 400, 500, 600, 700)),
                gpa REAL NOT NULL CHECK (gpa >= 0.0 AND gpa <= 4.0),
                email TEXT NOT NULL,
                phone_number TEXT NOT NULL,
                date_added TEXT NOT NULL,
                status TEXT NOT NULL CHECK (status IN ('Active', 'Inactive'))
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}