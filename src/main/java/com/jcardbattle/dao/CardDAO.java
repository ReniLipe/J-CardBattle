package com.jcardbattle.dao;

import com.jcardbattle.model.Card;
import java.util.List;

public interface CardDAO {
    // Metodo esistente
    List<Card> getAllCards();

    // NUOVO METODO: Carica un mazzo specifico (Solo la firma!)
    List<Card> loadDeck(int deckId);
}