package com.jcardbattle.model;

public class MinionCard extends Card {

    public MinionCard(int id, String name, String color, int cost, String desc, int atk, int hp) {
        // Aggiunto 'color' al super
        super(id, name, CardType.CREATURE, color, cost, atk, hp, desc);
    }

    public int getManaCost() {
        return getCost();
    }
}