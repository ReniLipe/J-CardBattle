package com.jcardbattle.dao;

import com.jcardbattle.model.*;
import java.util.ArrayList;
import java.util.List;

public class CardDAOImpl implements CardDAO {

    // Lista locale per testare senza database
    private List<Card> cards;

    public CardDAOImpl() {
        cards = new ArrayList<>();

        // Aggiungiamo carte manualmente usando le classi che abbiamo appena corretto
        // 1. Un Mostro (Minion)
        cards.add(new MinionCard(1, "Drago Rosso", 5, "Sputa fuoco", 2000, 1500));

        // 2. Una Magia (Spell) - Noterai che avrà 0/0 come stats nella grafica
        cards.add(new SpellCard(2, "Fulmine", 3, "Infligge danni"));

        // 3. Un altro Mostro
        cards.add(new MinionCard(3, "Goblin", 1, "Piccolo e veloce", 500, 400));

        // 4. Una Terra
        cards.add(new LandCard(4, "Palude", "Terreno fangoso"));

        // 5. Il Boss
        cards.add(new MinionCard(5, "Cavaliere Nero", 7, "Imbattibile", 2500, 2500));
    }

    @Override
    public List<Card> getAllCards() {
        return cards;
    }

    // Metodo helper opzionale
    public Card getCardByName(String name) {
        return cards.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}