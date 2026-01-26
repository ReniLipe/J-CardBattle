package com.jcardbattle.model;

public class LandCard extends Card {

    public LandCard(int id, String name, String color, String desc) {
        // Aggiunto 'color' al super. Cost, Atk, Def sono 0.
        super(id, name, CardType.LAND, color, 0, 0, 0, desc);
    }
}