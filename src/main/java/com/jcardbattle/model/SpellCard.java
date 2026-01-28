package com.jcardbattle.model;

public class SpellCard extends Card {

    public SpellCard(int id, String name, String color, int cost, String desc) {
        // Aggiunto 'color' al super. Atk e Def sono 0.
        super(id, name, CardType.SPELL, color, cost, 0, 0, desc);
    }

    public int getManaCost() {
        return getCost();
    }
}