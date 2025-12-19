package com.jcardbattle.model;

public class Card {
    private String nome;
    private int costoMana;
    private int attacco;

    public Card(String nome, int costoMana, int attacco) {
        this.nome = nome;
        this.costoMana = costoMana;
        this.attacco = attacco;
    }

    @Override
    public String toString() {
        return nome + " (Mana: " + costoMana + ", ATK: " + attacco + ")";
    }

    // Qui poi aggiungerai i Getter e Setter
}