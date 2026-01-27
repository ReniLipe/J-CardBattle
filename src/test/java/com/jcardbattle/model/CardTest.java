package com.jcardbattle.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testCardCreationAndGetters() {
        // 1. ARRANGE (Preparo i dati)
        int id = 1;
        String name = "Goblin Furioso";
        CardType type = CardType.CREATURE;
        String color = "RED";
        int cost = 2;
        int atk = 3;
        int def = 1;
        String desc = "Waaagh!";

        // 2. ACT (Creo l'oggetto)
        Card card = new Card(id, name, type, color, cost, atk, def, desc);

        // 3. ASSERT (Verifico che l'oggetto abbia memorizzato i dati giusti)
        // assertEquals(ValoreAtteso, ValoreReale)
        assertEquals(1, card.getId());
        assertEquals("Goblin Furioso", card.getName());
        assertEquals("RED", card.getColor());
        assertEquals(3, card.getAttack());
    }

    @Test
    void testCardToString() {
        // Testo che il metodo toString ritorni il nome
        Card card = new Card();
        card.setName("Fulmine");

        assertEquals("Fulmine", card.toString());
    }
}