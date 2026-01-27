package com.jcardbattle.dao;

import com.jcardbattle.model.Card;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseIntegrationTest {

    @BeforeAll
    static void initFreshDatabase() {
        // 1. Cancelliamo il vecchio DB per essere sicuri di partire da zero
        File dbFile = new File("jcardbattle.db");
        if (dbFile.exists()) {
            dbFile.delete();
            System.out.println("Vecchio DB cancellato per il test.");
        }

        // 2. Inizializziamo (Crea tabelle e popola carte)
        DbInitializer.initialize();
    }

    @Test
    void testDataPersistence() {
        // 3. Usiamo il DAO per leggere
        CardDAOImpl dao = new CardDAOImpl();
        List<Card> deck = dao.loadDeck(1);

        // 4. Verifiche
        assertNotNull(deck, "Il mazzo non deve essere null");
        assertFalse(deck.isEmpty(), "Il database dovrebbe contenere carte");

        // Verifica che ci siano esattamente le carte inserite nel DbInitializer (circa 40)
        assertEquals(40, deck.size(), "Dovrebbero esserci 40 carte starter");

        // Verifica contenuto specifico
        boolean foundGoblin = false;
        boolean foundIsland = false;

        for (Card c : deck) {
            if (c.getName().equals("Goblin Furioso") && c.getColor().equals("RED")) foundGoblin = true;
            if (c.getName().equals("Isola") && c.getColor().equals("BLUE")) foundIsland = true;
        }

        assertTrue(foundGoblin, "Il Goblin Furioso Rosso deve esistere");
        assertTrue(foundIsland, "L'Isola Blu deve esistere");
    }
}