package com.jcardbattle;

import com.jcardbattle.model.Card;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- J-CardBattle Engine Started ---");

        // Test veloce per vedere se il model funziona
        Card testCard = new Card("Drago Rosso", 5, 10);
        System.out.println("Carta creata: " + testCard);
    }
}