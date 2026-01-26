package com.jcardbattle.dao;

import com.jcardbattle.model.Card;
import com.jcardbattle.model.CardType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CardDAOImpl {

    public List<Card> loadDeck(int deckId) {
        List<Card> deck = new ArrayList<>();
        // Seleziona tutte le colonne, inclusa la nuova 'color'
        String sql = "SELECT * FROM cards";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Creiamo la carta vuota
                Card card = new Card();

                // Carichiamo i dati standard
                card.setId(rs.getInt("id"));
                card.setName(rs.getString("name"));
                card.setDescription(rs.getString("description"));
                card.setAttack(rs.getInt("attack"));
                card.setDefense(rs.getInt("defense"));
                card.setCost(rs.getInt("cost"));

                // --- NUOVA PARTE: CARICAMENTO COLORE ---
                String colorStr = rs.getString("color");
                if (colorStr != null && !colorStr.isEmpty()) {
                    card.setColor(colorStr);
                } else {
                    card.setColor("GRAY"); // Valore di default se manca nel DB
                }
                // ---------------------------------------

                // Gestione Tipo (Stringa -> Enum)
                String typeStr = rs.getString("type");
                try {
                    if (typeStr != null) {
                        card.setType(CardType.valueOf(typeStr.toUpperCase()));
                    } else {
                        card.setType(CardType.CREATURE); // Default
                    }
                } catch (Exception e) {
                    card.setType(CardType.CREATURE); // Fallback se errore
                }

                deck.add(card);
            }
            System.out.println("Caricate " + deck.size() + " carte.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deck;
    }
}