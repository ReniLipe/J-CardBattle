package com.jcardbattle.dao;

import com.jcardbattle.model.*;

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
                int id = rs.getInt("id");
                String name = rs.getString("name");
                CardType type = CardType.valueOf(rs.getString("card_type"));
                int cost = rs.getInt("mana_cost");
                String desc = rs.getString("description");
                int atk = rs.getInt("attack");
                int hp = rs.getInt("health");

                Card card = null;

                switch (type) {
                    case MINION:
                        card = new MinionCard(id, name, cost, desc, atk, hp);
                        break;
                    case LAND:
                        card = new LandCard(id, name, desc);
                        break;
                    case SPELL:
                        card = new SpellCard(id, name, cost, desc);
                        break;
                }

                if (card != null) {
                    cards.add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'errore se c'è
        }

        return cards;
    }
}