package com.jcardbattle.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardInheritanceTest {

    @Test
    void testMinionCard() {
        // Costruttore: ID, Nome, Colore, Costo, Descrizione, Atk, Hp
        MinionCard minion = new MinionCard(1, "Goblin", "RED", 2, "Attacca!", 3, 1);

        assertEquals("Goblin", minion.getName());
        assertEquals(CardType.CREATURE, minion.getType(), "MinionCard deve essere CREATURE");
        assertEquals("RED", minion.getColor());
        assertEquals(3, minion.getAttack());
        assertEquals(1, minion.getDefense());
    }

    @Test
    void testSpellCard() {
        // Costruttore: ID, Nome, Colore, Costo, Descrizione
        SpellCard spell = new SpellCard(2, "Fulmine", "RED", 1, "3 Danni");

        assertEquals(CardType.SPELL, spell.getType(), "SpellCard deve essere SPELL");
        assertEquals(0, spell.getAttack(), "Le spell non hanno attacco");
        assertEquals(1, spell.getManaCost());
    }

    @Test
    void testLandCard() {
        // Costruttore: ID, Nome, Colore, Descrizione
        LandCard land = new LandCard(3, "Isola", "BLUE", "Mana Blu");

        assertEquals(CardType.LAND, land.getType(), "LandCard deve essere LAND");
        assertEquals(0, land.getCost(), "Le terre costano 0");
        assertEquals("BLUE", land.getColor());
    }
}