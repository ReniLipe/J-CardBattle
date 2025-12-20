package com.jcardbattle.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/jcardbattle_db";
    private static final String USER = "root";
    private static final String PASSWORD = ".P4ssW0rd_?"; // La tua password

    public static Connection getConnection() throws SQLException {
        // --- AGGIUNTA FONDAMENTALE PER JAVAFX ---
        try {
            // Questo comando "sveglia" il driver manualmente
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trovato! Controlla il pom.xml");
            e.printStackTrace();
        }
        // ----------------------------------------

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}