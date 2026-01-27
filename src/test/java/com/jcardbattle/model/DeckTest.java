package com.jcardbattle.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    private Deck deck;
    private List<Card> cards;

    // @BeforeEach esegue questo codice PRIMA di ogni singolo test.
    // Serve a resettare la situazione per avere un mazzo pulito ogni volta.
    @BeforeEach
    void setUp() {
        cards = new ArrayList<>();
        cards.add(new Card(1, "C1", CardType.LAND, "RED", 0, 0, 0, ""));
        cards.add(new Card(2, "C2", CardType.CREATURE, "BLUE", 1, 1, 1, ""));
        cards.add(new Card(3, "C3", CardType.SPELL, "RED", 2, 0, 0, ""));

        deck = new Deck(cards);
    }

    @Test
    void testDeckSize() {
        // Appena creato, il mazzo deve avere 3 carte
        assertEquals(3, deck.size());
    }

    @Test
    void testDraw() {
        // 1. Pesco una carta
        Card drawn = deck.draw();

        // 2. Verifiche
        assertNotNull(drawn, "La carta pescata non dovrebbe essere null");
        assertEquals(2, deck.size(), "Il mazzo dovrebbe avere una carta in meno");
    }

    @Test
    void testDrawEmptyDeck() {
        // Svuoto il mazzo (pesco 3 volte)
        deck.draw();
        deck.draw();
        deck.draw();

        assertEquals(0, deck.size());

        // Ora provo a pescare dal mazzo vuoto
        Card emptyDraw = deck.draw();

        // Dovrebbe restituire null (o gestire l'errore, in base a come abbiamo scritto Deck)
        assertNull(emptyDraw, "Pescare da mazzo vuoto dovrebbe restituire null");
    }

    @Test
    void testSearch() {
        // Cerco una carta che esiste
        Card found = deck.search("C2");
        assertNotNull(found);
        assertEquals("C2", found.getName());

        // La carta cercata deve essere rimossa dal mazzo
        assertEquals(2, deck.size());
    }
}