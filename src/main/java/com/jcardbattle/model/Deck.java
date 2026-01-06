package com.jcardbattle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Deck {
    private List<Card> cards;

    public Deck(List<Card> initialCards) {
        this.cards = new ArrayList<>(initialCards);
    }

    // 1. MESCOLA
    public void shuffle() {
        Collections.shuffle(this.cards);
    }

    // 2. PESCA (Rimuove la prima carta)
    public Card draw() {
        if (cards.isEmpty()) {
            return null; // Mazzo finito
        }
        return cards.remove(0);
    }

    // 3. CERCA
    public Card search(String name) {
        Optional<Card> found = cards.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();

        if (found.isPresent()) {
            cards.remove(found.get());
            return found.get();
        }
        return null;
    }

    // RIMETTI NEL AZZO
    public void add(Card card) {
        cards.add(card);
    }

    public int size() {
        return cards.size();
    }
}