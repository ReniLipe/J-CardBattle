package com.jcardbattle.model;

// Aggiungi "abstract" qui
public abstract class Card {
    protected int id;
    protected String name;
    protected CardType type;
    protected int manaCost;
    protected String description;

    // Togli attack e health da qui! Li mettiamo solo nei Minion.
    // (Oppure lasciali se vuoi fare prima, ma pulito è meglio toglierli)

    // Costruttore Base
    public Card(int id, String name, CardType type, int manaCost, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.manaCost = manaCost;
        this.description = description;
    }

    // Ogni carta deve implementare questo metodo, ma ognuna lo farà a modo suo
    public abstract void play();

    // ... lascia i Getters e Setters (tranne attack/health se li togli)
    public String toString() {
        return name + " (" + type + ")";
    }
}