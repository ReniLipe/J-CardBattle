package com.jcardbattle.dao;

import com.jcardbattle.model.Card;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CardDAOImplTest {

    // @BeforeAll esegue questo metodo UNA volta sola prima di tutti i test.
    // Serve per assicurarci che il Database esista e sia popolato.
    @BeforeAll
    static void setupDatabase() {
        System.out.println("--- Setup Test Database ---");
        // Inizializza il DB (crea tabelle e inserisce carte se mancano)
        DbInitializer.initialize();
    }

    @Test
    void testLoadDeckConnection() {
        System.out.println("Test 1: Verifica caricamento mazzo");

        CardDAOImpl dao = new CardDAOImpl();
        List<Card> deck = dao.loadDeck(1);

        // 1. Il mazzo non deve essere nullo
        assertNotNull(deck, "La lista del mazzo non deve essere null");

        // 2. Il mazzo non deve essere vuoto (dovrebbe averne 40 se DbInitializer ha funzionato)
        assertFalse(deck.isEmpty(), "Il mazzo non dovrebbe essere vuoto");

        // 3. Verifichiamo il numero esatto (opzionale, ma consigliato)
        assertEquals(40, deck.size(), "Il mazzo dovrebbe contenere 40 carte");
    }

    @Test
    void testCardColors() {
        System.out.println("Test 2: Verifica colonna Colore");

        CardDAOImpl dao = new CardDAOImpl();
        List<Card> deck = dao.loadDeck(1);

        // Prendo una carta a caso (o le scorro tutte) per vedere se il colore è stato letto
        boolean foundRed = false;
        boolean foundBlue = false;

        for (Card c : deck) {
            assertNotNull(c.getColor(), "Il colore della carta non deve mai essere null (dovrebbe essere GRAY di default)");

            if (c.getColor().equals("RED")) foundRed = true;
            if (c.getColor().equals("BLUE")) foundBlue = true;

            // Stampiamo per debug visivo
            // System.out.println(c.getName() + " -> " + c.getColor());
        }

        // Verifico che ci siano carte rosse e blu nel mazzo
        assertTrue(foundRed, "Dovrebbero esserci carte ROSSE nel database");
        assertTrue(foundBlue, "Dovrebbero esserci carte BLU nel database");
    }
}