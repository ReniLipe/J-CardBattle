package com.jcardbattle.model;

public class Card {
    // Queste variabili DEVONO corrispondere alle colonne del DB
    private int id;
    private String name;
    private CardType type; // Qui usiamo l'Enum creato prima
    private int manaCost;
    private int attack;
    private int health;
    private String description;

    // Costruttore vuoto (Serve ai framework e per comodità)
    public Card() {}

    // Costruttore completo
    public Card(int id, String name, CardType type, int manaCost, int attack, int health, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.manaCost = manaCost;
        this.attack = attack;
        this.health = health;
        this.description = description;
    }

    // --- GETTERS & SETTERS (Il modo standard per leggere/scrivere i dati) ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CardType getType() { return type; }
    public void setType(CardType type) { this.type = type; }

    public int getManaCost() { return manaCost; }
    public void setManaCost(int manaCost) { this.manaCost = manaCost; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Questo serve per stampare la carta in console e vedere se funziona
    @Override
    public String toString() {
        return "Carta[" + id + "]: " + name + " (" + type + ") - Costo: " + manaCost;
    }
}