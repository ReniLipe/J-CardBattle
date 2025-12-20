package com.jcardbattle.model;

public class SpellCard extends Card {
    private int id;
    private int manaCost;
    private String description;

    public SpellCard(int id, String name, int cost, String desc) {
        // Passiamo 0 e 0 perché le magie non combattono fisicamente
        super(name, CardType.SPELL, 0, 0);

        this.id = id;
        this.manaCost = cost;
        this.description = desc;
    }

    public int getManaCost() { return manaCost; }
    public String getDescription() { return description; }
}