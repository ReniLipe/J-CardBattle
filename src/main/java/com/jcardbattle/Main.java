package com.jcardbattle;

import com.jcardbattle.dao.CardDAO;
import com.jcardbattle.dao.CardDAOImpl;
import com.jcardbattle.model.Card;
import com.jcardbattle.view.CardUI;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    // Variabile per ricordare chi stiamo trascinando
    private static Node draggedCard;
    // Variabile per ricordare qual è la zona della mano (per impedire il tap in mano)
    private Pane handZoneReference;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        // Sfondo generale dell'applicazione (per evitare bordi bianchi in fullscreen)
        root.setStyle("-fx-background-color: #2c3e50;");

        // ---------------------------------------------------------
        // 1. IL TAVOLO (Diviso in Righe)
        // ---------------------------------------------------------
        VBox boardContainer = new VBox(20); // Spazio verticale tra le righe
        boardContainer.setPadding(new Insets(20));
        boardContainer.setAlignment(Pos.CENTER);

        // --- RIGA 1: ZONA COMBATTIMENTO ---
        HBox combatRow = new HBox(15);
        combatRow.setPrefHeight(200); // Più alto per vedere bene le carte
        combatRow.setPrefWidth(900);
        combatRow.setAlignment(Pos.CENTER);
        combatRow.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-color: rgba(231, 76, 60, 0.1); -fx-background-radius: 10; -fx-border-radius: 10;");

        Label combatLabel = new Label("⚔️ ZONA COMBATTIMENTO");
        combatLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
        VBox combatWrapper = new VBox(5, combatLabel, combatRow);
        combatWrapper.setAlignment(Pos.CENTER);

        // --- RIGA 2: ZONA TERRE ---
        HBox landRow = new HBox(15);
        landRow.setPrefHeight(200);
        landRow.setPrefWidth(900);
        landRow.setAlignment(Pos.CENTER);
        landRow.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-background-color: rgba(39, 174, 96, 0.1); -fx-background-radius: 10; -fx-border-radius: 10;");

        Label landLabel = new Label("🌲 ZONA TERRE");
        landLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
        VBox landWrapper = new VBox(5, landLabel, landRow);
        landWrapper.setAlignment(Pos.CENTER);

        boardContainer.getChildren().addAll(combatWrapper, landWrapper);

        // ---------------------------------------------------------
        // 2. LA MANO DEL GIOCATORE
        // ---------------------------------------------------------
        HBox handZone = new HBox(-40); // Carte sovrapposte a ventaglio
        handZone.setPadding(new Insets(20, 20, 40, 80)); // Un po' più di margine sotto per il fullscreen
        handZone.setPrefHeight(250);
        handZone.setAlignment(Pos.BOTTOM_CENTER);
        handZone.setStyle("-fx-background-color: #34495e; -fx-border-color: #1abc9c; -fx-border-width: 3 0 0 0;");

        this.handZoneReference = handZone;

        // ---------------------------------------------------------
        // 3. ABILITIAMO LE ZONE (Tutte possono ricevere carte)
        // ---------------------------------------------------------
        enableDropZone(combatRow);
        enableDropZone(landRow);
        enableDropZone(handZone);

        // ---------------------------------------------------------
        // 4. CARICAMENTO CARTE
        // ---------------------------------------------------------
        CardDAO cardDAO = new CardDAOImpl();
        for (Card card : cardDAO.getAllCards()) {
            VBox cardNode = new CardUI(card).createCardNode();
            setupCardInteractions(cardNode, card);
            handZone.getChildren().add(cardNode);
        }

        root.setCenter(boardContainer);
        root.setBottom(handZone);

        // ---------------------------------------------------------
        // 5. SETUP SCENA E FULLSCREEN
        // ---------------------------------------------------------
        // Creiamo la scena senza dimensioni fisse, si adatterà allo schermo
        Scene scene = new Scene(root);

        // Gestione tastiera per uscire/entrare dal fullscreen
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            } else if (event.getCode() == KeyCode.ESCAPE) {
                // ESC lo gestisce JavaFX di default, ma possiamo aggiungere logica se serve
            }
        });

        primaryStage.setTitle("JCardBattle - Arena");
        primaryStage.setScene(scene);

        // ATTIVAZIONE SCHERMO INTERO
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("Premi 'ESC' per uscire o 'F' per cambiare modalità");

        primaryStage.show();
    }

    /**
     * Configura Drag & Drop E il Tapping (Rotazione)
     */
    private void setupCardInteractions(VBox cardNode, Card card) {

        // 1. ANIMAZIONE HOVER (Salto)
        cardNode.setOnMouseEntered(e -> {
            if (cardNode.getRotate() == 0) { // Salta solo se dritta
                cardNode.setViewOrder(-1);
                cardNode.setTranslateY(-40); // Salta più in alto
            }
        });

        cardNode.setOnMouseExited(e -> {
            cardNode.setViewOrder(0);
            cardNode.setTranslateY(0);
        });

        // 2. LOGICA DI TAPPING (Rotazione) - TASTO DESTRO
        cardNode.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) { // Tasto Destro

                // Non si tappa in mano
                if (cardNode.getParent() == handZoneReference) {
                    return;
                }

                // Logica TAP / UNTAP
                if (cardNode.getRotate() == 0) {
                    cardNode.setRotate(90);
                } else {
                    cardNode.setRotate(0);
                }
            }
        });

        // 3. LOGICA DRAG & DROP - TASTO SINISTRO
        cardNode.setOnDragDetected(event -> {
            // Non si sposta una carta tappata
            if (cardNode.getRotate() != 0) {
                event.consume();
                return;
            }

            draggedCard = cardNode;

            Dragboard db = cardNode.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(card.getName());
            db.setContent(content);

            // Snapshot Fantasma
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            WritableImage snapshot = cardNode.snapshot(params, null);

            // Centriamo il cursore sulla carta fantasma
            db.setDragView(snapshot, snapshot.getWidth()/2, snapshot.getHeight()/2);

            cardNode.setVisible(false);
            event.consume();
        });

        // Se il drag fallisce o finisce
        cardNode.setOnDragDone(event -> {
            if (event.getTransferMode() != TransferMode.MOVE) {
                cardNode.setVisible(true); // Rendila di nuovo visibile se non è stata spostata
            }
            event.consume();
        });
    }

    /**
     * Rende un pannello capace di ricevere carte
     */
    private void enableDropZone(Pane zone) {
        zone.setOnDragOver(event -> {
            if (event.getGestureSource() != zone && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        zone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && draggedCard != null) {
                // Rimuovi dal vecchio
                ((Pane) draggedCard.getParent()).getChildren().remove(draggedCard);
                // Aggiungi al nuovo
                zone.getChildren().add(draggedCard);

                // Reset completo grafico
                draggedCard.setVisible(true);
                draggedCard.setTranslateY(0);
                draggedCard.setViewOrder(0);
                draggedCard.setRotate(0); // Stappa automaticamente quando la sposti

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}