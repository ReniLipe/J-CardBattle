package com.jcardbattle.model;

public class LandCard extends Card {
    private int id;
    private String description;

    public LandCard(int id, String name, String desc) {
        // Passiamo 0 e 0 anche qui
        super(name, CardType.LAND, 0, 0);

        this.id = id;
        this.description = desc;
    }

    public String getDescription() { return description; }
}