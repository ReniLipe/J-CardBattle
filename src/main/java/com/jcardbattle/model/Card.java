package com.jcardbattle.model;

public class Card {
    private String name;
    private CardType type;
    private int attack;   // NUOVO!
    private int defense;  // NUOVO!

    // Costruttore aggiornato
    public Card(String name, CardType type, int attack, int defense) {
        this.name = name;
        this.type = type;
        this.attack = attack;
        this.defense = defense;
    }

    // Getters
    public String getName() {
        return name;
    }

    public CardType getType() {
        return type;
    }

    // QUESTI MANCAVANO!
    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }
}