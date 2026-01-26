package com.jcardbattle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class DbInitializer {

    public static void initialize() {
        // AGGIUNTO 'color TEXT'
        String createTableSQL = "CREATE TABLE IF NOT EXISTS cards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "type TEXT, " +
                "color TEXT, " +  // <--- NUOVA COLONNA
                "attack INTEGER, " +
                "defense INTEGER, " +
                "cost INTEGER, " +
                "description TEXT" +
                ");";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);

            if (isTableEmpty(conn)) {
                System.out.println("Creazione Mazzo Izzet con Colori...");
                insertStarterCards(conn);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isTableEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM cards")) {
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (Exception e) { }
        return true;
    }

    private static void insertStarterCards(Connection conn) {
        // AGGIUNTO ? per il colore
        String insertSQL = "INSERT INTO cards (name, type, color, attack, defense, cost, description) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            // TERRE
            for(int i=0; i<10; i++) addCard(pstmt, "Montagna", "LAND", "RED", 0, 0, 0, "Mana Rosso.");
            for(int i=0; i<10; i++) addCard(pstmt, "Isola", "LAND", "BLUE", 0, 0, 0, "Mana Blu.");

            // CREATURE ROSSE
            for(int i=0; i<4; i++) addCard(pstmt, "Goblin Furioso", "CREATURE", "RED", 2, 1, 1, "Attacca subito.");
            for(int i=0; i<2; i++) addCard(pstmt, "Drago Vulcanico", "CREATURE", "RED", 5, 4, 5, "Volare, Rapidità.");

            // CREATURE BLU
            for(int i=0; i<4; i++) addCard(pstmt, "Mago del Vento", "CREATURE", "BLUE", 2, 2, 3, "Volare. Pesca 1.");

            // INCOLORE
            for(int i=0; i<2; i++) addCard(pstmt, "Golem di Ferro", "CREATURE", "GRAY", 4, 4, 4, "Artefatto solido.");

            // SPELLS ROSSE
            for(int i=0; i<4; i++) addCard(pstmt, "Fulmine", "SPELL", "RED", 0, 0, 1, "3 Danni.");

            // SPELLS BLU
            for(int i=0; i<4; i++) addCard(pstmt, "Studio Arcano", "SPELL", "BLUE", 0, 0, 3, "Pesca 2 carte.");

            pstmt.executeBatch();
            System.out.println("Carte inserite con successo!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo aggiornato con parametro COLOR
    private static void addCard(PreparedStatement pstmt, String name, String type, String color, int atk, int def, int cost, String desc) throws Exception {
        pstmt.setString(1, name);
        pstmt.setString(2, type);
        pstmt.setString(3, color); // <--- INSERISCE COLORE
        pstmt.setInt(4, atk);
        pstmt.setInt(5, def);
        pstmt.setInt(6, cost);
        pstmt.setString(7, desc);
        pstmt.addBatch();
    }
}