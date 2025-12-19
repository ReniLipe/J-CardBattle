package com.jcardbattle.model;

public class MinionCard extends Card {
    private int attack;
    private int health;

    public MinionCard(int id, String name, int manaCost, String desc, int attack, int health) {
        super(id, name, CardType.MINION, manaCost, desc);
        this.attack = attack;
        this.health = health;
    }

    @Override
    public void play() {
        System.out.println(">>> EVOCAZIONE: " + name + " scende in campo con " + attack + "/" + health);
        // Qui in futuro metteremo la logica per aggiungerlo al tavolo
    }

    public int getAttack() { return attack; }
    public int getHealth() { return health; }
}