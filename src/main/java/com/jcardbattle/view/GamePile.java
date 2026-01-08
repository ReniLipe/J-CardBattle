package com.jcardbattle.view;

import com.jcardbattle.model.Card;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class GamePile extends StackPane {

    private final String zoneName;
    private final Label countLabel;
    private final List<Card> cards = new ArrayList<>();

    public GamePile(String displayName, String zoneId, String colorHex) {
        this.zoneName = zoneId;

        this.setPrefSize(100, 140);
        this.setMaxSize(100, 140);
        this.setStyle("-fx-border-color: " + colorHex + "; -fx-border-width: 3; -fx-border-radius: 8; -fx-background-color: rgba(0,0,0,0.3); -fx-cursor: hand;");
        this.setAlignment(Pos.CENTER);

        Label titleLbl = new Label(displayName);
        titleLbl.setTextFill(Color.web(colorHex));
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(90);
        titleLbl.setAlignment(Pos.CENTER);

        countLabel = new Label("0");
        countLabel.setTextFill(Color.WHITE);
        countLabel.setStyle("-fx-background-color: black; -fx-padding: 2 5; -fx-background-radius: 5;");
        StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);

        this.getChildren().addAll(titleLbl, countLabel);
        Tooltip.install(this, new Tooltip(displayName + " (Tasto DX per aprire)"));
    }

    public void addCard(Card card) {
        cards.add(card);
        addVisualCard(card.getName());
    }

    public void addVisualCard(String cardName) {
        Platform.runLater(() -> {
            StackPane cardNode = new StackPane();
            cardNode.setPrefSize(90, 130);
            Rectangle bg = new Rectangle(90, 130);
            bg.setFill(Color.LIGHTGRAY); bg.setStroke(Color.BLACK);
            Label lbl = new Label(cardName);
            lbl.setWrapText(true); lbl.setMaxWidth(80); lbl.setAlignment(Pos.CENTER);

            cardNode.getChildren().addAll(bg, lbl);
            this.getChildren().add(cardNode);
            countLabel.toFront();
            updateCount();
        });
    }

    public void removeCard(Card card) {
        cards.remove(card);
        removeTopCard();
    }

    public void removeTopCard() {
        Platform.runLater(() -> {
            for (int i = this.getChildren().size() - 1; i >= 0; i--) {
                Node n = this.getChildren().get(i);
                if (n instanceof StackPane) {
                    this.getChildren().remove(i);
                    break;
                }
            }
            updateCount();
        });
    }

    public void clear() {
        cards.clear();
        Platform.runLater(() -> {
            this.getChildren().removeIf(node -> node instanceof StackPane);
            updateCount();
        });
    }

    private void updateCount() {
        long count = this.getChildren().stream().filter(n -> n instanceof StackPane).count();
        countLabel.setText(String.valueOf(count));
    }

    public String getZoneName() { return zoneName; }
    public List<Card> getCards() { return cards; }
}