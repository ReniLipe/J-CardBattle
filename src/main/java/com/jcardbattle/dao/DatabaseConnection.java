package com.jcardbattle.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // --- CORREZIONE QUI SOTTO ---
    // Aggiunto ":3306" (porta) e "/sql7813505" (nome del database)
    private static final String URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7813505";

    private static final String USER = "sql7813505";
    private static final String PASSWORD = "dZsT3mIRxw";

    public static Connection getConnection() throws SQLException {
        try {
            // Caricamento manuale del driver (essenziale per JavaFX/Fat JAR)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trovato! Controlla il pom.xml");
            e.printStackTrace();
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}