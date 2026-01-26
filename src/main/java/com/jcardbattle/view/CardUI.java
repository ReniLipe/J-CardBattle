package com.jcardbattle.view;

import com.jcardbattle.model.Card;
import com.jcardbattle.model.CardType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class CardUI {

    private final Card card;

    public CardUI(Card card) {
        this.card = card;
    }

    public VBox createCardNode(boolean isBig) {

        double width = isBig ? 220 : 100;
        double height = isBig ? 300 : 140;
        double titleSize = isBig ? 14 : 9;
        double typeSize = isBig ? 11 : 7;
        double descSize = isBig ? 12 : 7;
        double ptSize = isBig ? 16 : 10;
        double costSize = isBig ? 14 : 10;
        double cornerRadius = isBig ? 15 : 8;

        // --- NUOVA LOGICA SEMPLIFICATA BASATA SUL DATABASE ---
        String frameColor;
        String bgColor;

        // Prendiamo il colore dal DB, se è null usiamo GRAY
        String dbColor = (card.getColor() != null) ? card.getColor().toUpperCase() : "GRAY";

        switch (dbColor) {
            case "RED":
                frameColor = "#e74c3c"; // Rosso acceso
                bgColor = "#c0392b";    // Rosso scuro
                break;
            case "BLUE":
                frameColor = "#3498db"; // Blu acceso
                bgColor = "#2980b9";    // Blu scuro
                break;
            case "GREEN":
                frameColor = "#2ecc71";
                bgColor = "#27ae60";
                break;
            case "BLACK":
                frameColor = "#9b59b6"; // Viola/Nero
                bgColor = "#8e44ad";
                break;
            case "WHITE":
                frameColor = "#f1c40f"; // Oro/Giallo
                bgColor = "#f39c12";
                break;
            case "GRAY":
            default:
                frameColor = "#bdc3c7"; // Grigio Artefatto
                bgColor = "#95a5a6";
                break;
        }

        // --- COSTRUZIONE GRAFICA (Uguale a prima) ---
        VBox cardBox = new VBox(isBig ? 4 : 2);
        cardBox.setPrefSize(width, height);
        cardBox.setMaxSize(width, height);

        cardBox.setStyle("-fx-background-color: " + frameColor + "; " +
                "-fx-background-radius: " + cornerRadius + "; " +
                "-fx-border-color: black; -fx-border-width: " + (isBig?2:1) + "; -fx-border-radius: " + cornerRadius + "; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), " + (isBig?10:5) + ", 0, 0, 0);");
        cardBox.setPadding(new Insets(isBig ? 8 : 4));

        // HEADER
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(isBig?2:0, isBig?5:2, isBig?2:0, isBig?5:2));
        titleBar.setPrefHeight(isBig ? 28 : 18);
        titleBar.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 4; -fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 4;");

        Label nameLbl = new Label(card.getName());
        nameLbl.setFont(Font.font("Georgia", FontWeight.BOLD, titleSize));
        nameLbl.setTextFill(Color.BLACK);
        if(!isBig) nameLbl.setMaxWidth(60);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane manaBubble = new StackPane();
        double circleSize = isBig ? 20 : 12;
        Rectangle mCircle = new Rectangle(circleSize, circleSize);
        mCircle.setArcWidth(circleSize); mCircle.setArcHeight(circleSize);
        mCircle.setFill(Color.web("#bdc3c7")); mCircle.setStroke(Color.BLACK);
        Label costLbl = new Label(String.valueOf(card.getCost()));
        costLbl.setFont(Font.font("Arial", FontWeight.BOLD, costSize));
        manaBubble.getChildren().addAll(mCircle, costLbl);
        titleBar.getChildren().addAll(nameLbl, spacer, manaBubble);

        // IMAGE
        StackPane imageBox = new StackPane();
        double imgHeight = isBig ? 130 : 55;
        Rectangle imgRect = new Rectangle(width - (isBig?18:10), imgHeight);
        imgRect.setFill(Color.web("#2c3e50"));
        imgRect.setStroke(Color.BLACK); imgRect.setStrokeWidth(1);
        Rectangle artShape = new Rectangle(isBig?100:40, isBig?60:25);
        artShape.setFill(Color.web(bgColor));
        imageBox.getChildren().addAll(imgRect, artShape);

        // TYPE
        HBox typeBar = new HBox();
        typeBar.setPadding(new Insets(0, isBig?5:2, 0, isBig?5:2));
        typeBar.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 3; -fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 3;");
        Label typeLbl = new Label(card.getType().toString());
        typeLbl.setFont(Font.font("Georgia", FontWeight.BOLD, typeSize));
        typeBar.getChildren().add(typeLbl);
        HBox.setHgrow(typeBar, Priority.ALWAYS);

        // TEXT
        VBox textBox = new VBox();
        textBox.setPadding(new Insets(isBig?5:2));
        textBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: black; -fx-border-width: 1;");
        Label descLbl = new Label(card.getDescription());
        descLbl.setWrapText(true);
        descLbl.setFont(Font.font("Georgia", descSize));
        descLbl.setTextAlignment(TextAlignment.LEFT);
        descLbl.setAlignment(Pos.TOP_LEFT);
        if (!isBig) { descLbl.setMaxHeight(35); textBox.setPrefHeight(35); }
        else { VBox.setVgrow(textBox, Priority.ALWAYS); }
        textBox.getChildren().add(descLbl);

        // FOOTER
        AnchorPane footer = new AnchorPane();
        if (card.getType() == CardType.CREATURE) {
            StackPane ptBox = new StackPane();
            double ptW = isBig ? 50 : 30;
            double ptH = isBig ? 25 : 15;
            Rectangle ptBg = new Rectangle(ptW, ptH);
            ptBg.setArcWidth(8); ptBg.setArcHeight(8);
            ptBg.setFill(Color.web("#ecf0f1"));
            ptBg.setStroke(Color.BLACK); ptBg.setStrokeWidth(isBig?2:1);
            Label ptLbl = new Label(card.getAttack() + "/" + card.getDefense());
            ptLbl.setFont(Font.font("Arial", FontWeight.BOLD, ptSize));
            ptBox.getChildren().addAll(ptBg, ptLbl);
            AnchorPane.setBottomAnchor(ptBox, isBig ? -5.0 : -2.0);
            AnchorPane.setRightAnchor(ptBox, 0.0);
            footer.getChildren().add(ptBox);
        }

        cardBox.getChildren().addAll(titleBar, imageBox, typeBar, textBox, footer);
        return cardBox;
    }
}