package com.jcardbattle.dao;

import com.jcardbattle.model.Card;
import com.jcardbattle.model.CardType; // Assicurati di avere questo Enum
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardDAOImpl implements CardDAO {

    // 1. METODO PRINCIPALE: Carica il mazzo dal DB
    // Questo metodo sostituisce la lista manuale che avevi prima.
    @Override
    public List<Card> loadDeck(int deckId) {
        List<Card> deckFromDb = new ArrayList<>();

        // La query SQL che unisce le carte alla composizione del mazzo
        String sql = "SELECT c.name, c.card_type, c.attack, c.health " +
                "FROM cards c " +
                "JOIN deck_composition dc ON c.id = dc.card_id " +
                "WHERE dc.deck_id = ?";

        // "Try-with-resources": chiude automaticamente la connessione alla fine
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deckId); // Sostituisce il '?' con l'ID (es. 1)

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Leggiamo i dati dalla riga SQL
                String nome = rs.getString("name");
                String tipoStringa = rs.getString("card_type"); // Es. "MINION"
                int attacco = rs.getInt("attack");
                int vita = rs.getInt("health"); // Nel DB è 'health', in Java lo usiamo come 'defense'

                // Convertiamo la stringa del DB (es. "MINION") nel tipo Java Enum
                // Se nel DB hai scritto "LAND", cercherà CardType.LAND
                CardType tipoEnum = CardType.valueOf(tipoStringa);

                // Creiamo l'oggetto Carta (usiamo la classe Card generica che mi hai mostrato prima)
                Card c = new Card(nome, tipoEnum, attacco, vita);

                deckFromDb.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel caricamento del mazzo dal DB!");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Errore: Il tipo di carta nel DB non corrisponde all'Enum Java!");
            e.printStackTrace();
        }

        return deckFromDb;
    }

    // 2. METODO SECONDARIO: (Opzionale)
    // Se vuoi ancora un metodo che prenda TUTTE le carte del gioco (per la collezione)
    @Override
    public List<Card> getAllCards() {
        List<Card> allCards = new ArrayList<>();
        String sql = "SELECT * FROM cards";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                CardType tipo = CardType.valueOf(rs.getString("card_type"));
                allCards.add(new Card(
                        rs.getString("name"),
                        tipo,
                        rs.getInt("attack"),
                        rs.getInt("health")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allCards;
    }
}