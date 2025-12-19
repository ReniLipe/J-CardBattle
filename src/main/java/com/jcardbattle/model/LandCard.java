package com.jcardbattle.model;

public class LandCard extends Card {
    public LandCard(int id, String name, String desc) {
        super(id, name, CardType.LAND, 0, desc); // Costo 0 sempre
    }

    @Override
    public void play() {
        System.out.println(">>> TERRA GIOCATA: " + name + " ti dà +1 Mana Permanente!");
        // Qui aumenteremo il mana del giocatore
    }
}