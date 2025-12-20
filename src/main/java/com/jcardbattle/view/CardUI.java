package com.jcardbattle.view;

import com.jcardbattle.model.Card;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;

public class CardUI {

    private Card card;

    public CardUI(Card card) {
        this.card = card;
    }

    // ECCO IL METODO CHE MANCAVA!
    public VBox createCardNode() {
        // 1. Creiamo il contenitore verticale (la carta fisica)
        VBox cardBox = new VBox(5); // 5px di spazio tra gli elementi
        cardBox.setPrefSize(100, 140); // Dimensioni fisse per la carta
        cardBox.setAlignment(Pos.CENTER);

        // Stile base della carta (Bordo arrotondato, colore di sfondo)
        String style = "-fx-background-color: white; " +
                "-fx-border-color: black; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5;";
        cardBox.setStyle(style);

        // Effetto ombra per renderla "carina"
        cardBox.setEffect(new DropShadow(5, Color.GRAY));

        // 2. Aggiungiamo i dati della carta
        Label nameLabel = new Label(card.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label typeLabel = new Label(card.getType().toString());
        typeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        // Un rettangolo colorato per bellezza (rappresenta l'immagine)
        Rectangle imagePlaceholder = new Rectangle(80, 50, Color.LIGHTBLUE);

        // Statistiche (Attacco / Difesa)
        Label statsLabel = new Label("⚔ " + card.getAttack() + " | 🛡 " + card.getDefense());
        statsLabel.setStyle("-fx-font-weight: bold;");

        // 3. Mettiamo tutto dentro il box
        cardBox.getChildren().addAll(nameLabel, typeLabel, imagePlaceholder, statsLabel);

        return cardBox;
    }
}