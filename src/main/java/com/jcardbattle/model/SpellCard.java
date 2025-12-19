package com.jcardbattle.model;

public class SpellCard extends Card {
    public SpellCard(int id, String name, int manaCost, String desc) {
        super(id, name, CardType.SPELL, manaCost, desc);
    }

    @Override
    public void play() {
        System.out.println(">>> MAGIA LANCIATA: " + name + " attiva il suo effetto: " + description);
        // Qui attiveremo l'effetto
    }
}