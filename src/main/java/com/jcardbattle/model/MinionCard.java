package com.jcardbattle.model;

public class MinionCard extends Card {
    private int id;
    private int manaCost;
    private String description;

    // Costruttore: deve passare atk e hp al genitore (Card)
    public MinionCard(int id, String name, int cost, String desc, int atk, int hp) {
        // SUPER è la chiamata al costruttore di Card.
        // Passiamo: Nome, Tipo, Attacco, Difesa (usiamo hp come difesa)
        super(name, CardType.MINION, atk, hp);

        this.id = id;
        this.manaCost = cost;
        this.description = desc;
    }

    // Getter specifici dei Minion
    public int getManaCost() { return manaCost; }
    public String getDescription() { return description; }
}