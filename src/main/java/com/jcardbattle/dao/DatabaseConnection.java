package com.jcardbattle.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // URL JDBC per SQLite: crea un file nella cartella del gioco
    private static final String URL = "jdbc:sqlite:jcardbattle.db";

    public static Connection getConnection() throws SQLException {
        try {
            // Carica il driver SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite non trovato! Controlla il pom.xml");
            e.printStackTrace();
        }
        // SQLite non richiede user e password
        return DriverManager.getConnection(URL);
    }
}