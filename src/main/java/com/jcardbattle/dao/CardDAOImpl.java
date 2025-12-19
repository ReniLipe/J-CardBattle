package com.jcardbattle.dao;

import com.jcardbattle.model.Card;
import com.jcardbattle.model.CardType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardDAOImpl implements CardDAO {

    @Override
    public List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards";

        // Usiamo la nostra classe DatabaseConnection
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Card c = new Card();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                // Conversione sicura della stringa in ENUM
                c.setType(CardType.valueOf(rs.getString("card_type")));
                c.setManaCost(rs.getInt("mana_cost"));
                c.setAttack(rs.getInt("attack"));
                c.setHealth(rs.getInt("health"));
                c.setDescription(rs.getString("description"));

                cards.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore se c'è
        }

        return cards;
    }
}