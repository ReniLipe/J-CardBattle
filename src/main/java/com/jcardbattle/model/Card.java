package com.jcardbattle.model;

public class Card {
    private int id;
    private String name;
    private String description;
    private CardType type;
    private int cost;
    private int attack;
    private int defense;

    // NUOVO CAMPO
    private String color;

    public Card() { }

    // Costruttore completo aggiornato
    public Card(int id, String name, CardType type, String color, int cost, int attack, int defense, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color; // <--- QUI
        this.cost = cost;
        this.attack = attack;
        this.defense = defense;
        this.description = description;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CardType getType() { return type; }
    public void setType(CardType type) { this.type = type; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    // NUOVI METODI PER IL COLORE
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public String toString() { return name; }
}