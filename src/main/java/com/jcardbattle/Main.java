package com.jcardbattle;

import com.jcardbattle.dao.CardDAOImpl;
import com.jcardbattle.model.Card;
import com.jcardbattle.model.Deck;
import com.jcardbattle.view.CardUI;
import com.jcardbattle.view.GameView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Collections;

public class Main extends Application {

    private GameView view;
    private static Node draggedCard; // Per il drag & drop

    // --- STATO DEL GIOCO ---
    private int lifePoints = 20;
    private Deck playerDeck;

    // FASI DEL TURNO
    private enum Phase { UNTAP, DRAW, MAIN1, COMBAT, MAIN2, END }
    private Phase currentPhase = Phase.MAIN1;
    private int turnCount = 1;

    // MANA POOL: [0]=White, [1]=Blue, [2]=Black, [3]=Red, [4]=Green, [5]=Colorless
    private int[] manaPool = {0, 0, 0, 0, 0, 0};

    @Override
    public void start(Stage primaryStage) {
        view = new GameView();

        setupEventHandlers(primaryStage);

        Scene scene = new Scene(view.getRoot(), 1024, 768);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F) primaryStage.setFullScreen(!primaryStage.isFullScreen());
        });

        primaryStage.setTitle("J-CardBattle - Magic Engine");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    // =================================================================
    // SETUP EVENTI (Collegamento View -> Controller)
    // =================================================================
    private void setupEventHandlers(Stage stage) {
        // MENU PRINCIPALE
        view.getBtnSinglePlayer().setOnAction(e -> avviaNuovaPartita());
        view.getBtnMultiPlayer().setOnAction(e -> view.log("Funzione Online in manutenzione..."));
        view.getBtnExit().setOnAction(e -> { Platform.exit(); System.exit(0); });

        // MENU DI GIOCO
        view.getBtnPause().setOnAction(e -> mostraMenuPausa());

        // VITA
        view.getBtnMinusLife().setOnAction(e -> {
            lifePoints--; view.updateLife(lifePoints); checkGameOver();
        });
        view.getBtnPlusLife().setOnAction(e -> {
            lifePoints++; view.updateLife(lifePoints);
        });

        // MAZZO & RICERCA
        view.getDeckVisual().setOnMouseClicked(e -> { view.animateDeckClick(); pescaCarta(); });
        view.getBtnShuffle().setOnAction(e -> {
            if(playerDeck != null) { playerDeck.shuffle(); view.log("Mazzo mescolato!"); view.animateDeckClick(); }
        });
        view.getBtnSearch().setOnAction(e -> eseguiRicerca());

        // TASTO PASSA FASE
        view.getBtnNextPhase().setOnAction(e -> avanzaFase());

        // --- GESTIONE MANA INTERATTIVO (CLICK SX: +, CLICK DX: -) ---
        Label[] manaLabels = view.getManaLabels();
        for (int i = 0; i < 6; i++) {
            final int colorIndex = i; // Necessario per la lambda
            manaLabels[i].setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    // Tasto Sinistro: AUMENTA
                    manaPool[colorIndex]++;
                    view.log("Mana +1 (" + getManaSymbol(colorIndex) + ")");
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    // Tasto Destro: DIMINUISCE
                    if (manaPool[colorIndex] > 0) {
                        manaPool[colorIndex]--;
                        view.log("Mana -1 (" + getManaSymbol(colorIndex) + ")");
                    }
                }
                // Aggiorna la grafica
                view.updateManaDisplay(manaPool);
            });
        }

        // DRAG & DROP
        enableDropZone(view.getHandZone());
        enableDropZone(view.getCombatRow());
        enableDropZone(view.getLandRow());
        enableDeckDropZone(view.getDeckVisual());
    }

    // =================================================================
    // LOGICA DI GIOCO: START & FASI
    // =================================================================
    private void avviaNuovaPartita() {
        // Reset Variabili
        lifePoints = 20;
        turnCount = 1;
        view.updateLife(lifePoints);
        view.getSearchField().clear();
        view.getHandZone().getChildren().clear();
        view.getCombatRow().getChildren().clear();
        view.getLandRow().getChildren().clear();

        // Reset Mana
        for(int i=0; i<6; i++) manaPool[i] = 0;
        view.updateManaDisplay(manaPool);

        // Carica Mazzo
        try {
            CardDAOImpl dao = new CardDAOImpl();
            playerDeck = new Deck(dao.loadDeck(1)); // Carica mazzo ID 1
            playerDeck.shuffle();
            view.updateDeckCount(playerDeck.size());
        } catch (Exception e) {
            view.log("Errore DB (Uso mazzo vuoto): " + e.getMessage());
            playerDeck = new Deck(Collections.emptyList());
        }

        // Pesca mano iniziale (7 carte)
        if (playerDeck.size() > 0) {
            for(int i=0; i<7; i++) pescaCarta();
        }

        // Inizia la partita in MAIN PHASE 1
        currentPhase = Phase.MAIN1;
        updatePhaseUI();
        view.showGame();
        view.log("--- PARTITA INIZIATA ---");
        view.log("Siamo nella MAIN PHASE 1.");
    }

    private void avanzaFase() {
        switch (currentPhase) {
            case UNTAP:
                currentPhase = Phase.DRAW;
                performDrawStep();
                break;
            case DRAW:
                currentPhase = Phase.MAIN1;
                view.log(">> MAIN PHASE 1");
                break;
            case MAIN1:
                currentPhase = Phase.COMBAT;
                view.log(">> COMBAT PHASE (Attacca!)");
                break;
            case COMBAT:
                currentPhase = Phase.MAIN2;
                view.log(">> MAIN PHASE 2");
                break;
            case MAIN2:
                currentPhase = Phase.END;
                view.log(">> END STEP");
                break;
            case END:
                // FINE TURNO -> INIZIO NUOVO TURNO
                passaTurno();
                break;
        }
        updatePhaseUI();
    }

    private void passaTurno() {
        turnCount++;
        view.log("=========================");
        view.log("--- INIZIO TURNO " + turnCount + " ---");

        currentPhase = Phase.UNTAP;
        view.log(">> UNTAP STEP");

        // 1. UNTAP: Ruota tutte le carte a 0 gradi (Stappa)
        view.getLandRow().getChildren().forEach(n -> {
            n.setRotate(0);
            n.setOpacity(1.0); // Rimuovi eventuali effetti visivi
        });
        view.getCombatRow().getChildren().forEach(n -> {
            n.setRotate(0);
            n.setOpacity(1.0);
        });

        view.log("Tutto stappato.");
    }

    private void performDrawStep() {
        view.log(">> DRAW STEP");
        pescaCarta();
    }

    private void updatePhaseUI() {
        String labelName = "";
        switch (currentPhase) {
            case UNTAP: labelName = "UNTAP"; break;
            case DRAW: labelName = "DRAW"; break;
            case MAIN1: labelName = "MAIN 1"; break;
            case COMBAT: labelName = "COMBAT"; break;
            case MAIN2: labelName = "MAIN 2"; break;
            case END: labelName = "END"; break;
        }
        view.highlightPhase(labelName);
    }

    // =================================================================
    // LOGICA CARTE (Pesca, Cerca, Add)
    // =================================================================
    private void pescaCarta() {
        if (playerDeck == null) return;
        Card c = playerDeck.draw();
        if (c != null) {
            addCardToZone(c, view.getHandZone());
            view.log("Pescato: " + c.getName());
            view.updateDeckCount(playerDeck.size());
        } else {
            view.log("Mazzo finito!");
            view.getDeckVisual().setOpacity(0.5);
        }
    }

    private void eseguiRicerca() {
        String query = view.getSearchField().getText().trim();
        if (query.isEmpty() || playerDeck == null) return;
        Card c = playerDeck.search(query);
        if (c != null) {
            addCardToZone(c, view.getHandZone());
            view.log("Trovato: " + c.getName());
            view.getSearchField().clear();
            view.updateDeckCount(playerDeck.size());
        } else {
            view.log("Nessuna carta trovata: " + query);
        }
    }

    private void checkGameOver() {
        if (lifePoints <= 0) mostraOverlayMessaggio("GAME OVER", false);
    }

    private String getManaSymbol(int index) {
        String[] names = {"Bianco", "Blu", "Nero", "Rosso", "Verde", "Incolore"};
        return (index >= 0 && index < names.length) ? names[index] : "?";
    }

    // =================================================================
    // GESTIONE GRAFICA CARTE & DRAG-DROP
    // =================================================================
    private void addCardToZone(Card card, Pane zone) {
        if (card == null) return;
        VBox cardNode = new CardUI(card).createCardNode();
        cardNode.setUserData(card); // Salva l'oggetto Card nel nodo grafico
        setupCardInteractions(cardNode);
        zone.getChildren().add(cardNode);
    }

    private void setupCardInteractions(VBox cardNode) {
        // Hover Effect
        cardNode.setOnMouseEntered(e -> {
            if (cardNode.getRotate() == 0) { cardNode.setViewOrder(-1); cardNode.setTranslateY(-40); }
        });
        cardNode.setOnMouseExited(e -> { cardNode.setViewOrder(0); cardNode.setTranslateY(0); });

        // Click Destro -> TAPPING
        cardNode.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                if (cardNode.getParent() != view.getHandZone()) {
                    cardNode.setRotate(cardNode.getRotate() == 0 ? 90 : 0);
                }
            }
        });

        // Inizio Drag
        cardNode.setOnDragDetected(event -> {
            if (cardNode.getRotate() != 0) return; // Non draggare se tappata
            draggedCard = cardNode;

            Dragboard db = cardNode.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent(); content.putString("card_move"); db.setContent(content);

            WritableImage snap = cardNode.snapshot(new SnapshotParameters(), null);
            db.setDragView(snap, snap.getWidth()/2, snap.getHeight()/2);

            cardNode.setVisible(false);
            event.consume();
        });

        // Fine Drag
        cardNode.setOnDragDone(event -> {
            if (event.getTransferMode() != TransferMode.MOVE) cardNode.setVisible(true);
            event.consume();
        });
    }

    private void enableDropZone(Pane zone) {
        zone.setOnDragOver(e -> { if (e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.MOVE); e.consume(); });
        zone.setOnDragDropped(e -> {
            if (draggedCard != null) {
                ((Pane)draggedCard.getParent()).getChildren().remove(draggedCard);
                zone.getChildren().add(draggedCard);
                draggedCard.setVisible(true); draggedCard.setTranslateY(0); draggedCard.setViewOrder(0); draggedCard.setRotate(0);
                e.setDropCompleted(true);
            } e.consume();
        });
    }

    // Ritorno al Mazzo
    private void enableDeckDropZone(StackPane deckVisual) {
        deckVisual.setOnDragOver(e -> { if (e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.MOVE); e.consume(); });
        deckVisual.setOnDragDropped(e -> {
            if (draggedCard != null) {
                Card cardObject = (Card) draggedCard.getUserData();
                if (cardObject != null) {
                    ((Pane) draggedCard.getParent()).getChildren().remove(draggedCard);
                    playerDeck.add(cardObject);
                    playerDeck.shuffle();

                    view.updateDeckCount(playerDeck.size());
                    view.log("♻ " + cardObject.getName() + " tornata nel mazzo.");
                    view.animateDeckClick();
                    e.setDropCompleted(true);
                }
            } e.consume();
        });
    }

    // =================================================================
    // OVERLAY
    // =================================================================
    private void mostraMenuPausa() {
        VBox content = new VBox(20); content.setAlignment(Pos.CENTER);
        Label lbl = new Label("PAUSA"); lbl.setFont(Font.font("Impact", 60)); lbl.setTextFill(Color.WHITE);
        Button btnResume = new Button("▶ RIPRENDI"); btnResume.setOnAction(e -> view.hideOverlay());
        Button btnMenu = new Button("🏠 MENU"); btnMenu.setOnAction(e -> view.showMenu());

        // Stile bottoni overlay
        String style = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 20px;";
        btnResume.setStyle(style);
        btnMenu.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 20px;");

        content.getChildren().addAll(lbl, btnResume, btnMenu); view.showOverlay(content);
    }

    private void mostraOverlayMessaggio(String testo, boolean resume) {
        VBox content = new VBox(20); content.setAlignment(Pos.CENTER);
        Label lbl = new Label(testo); lbl.setFont(Font.font("Impact", 60)); lbl.setTextFill(Color.RED);
        Button btnMenu = new Button("🏠 MENU"); btnMenu.setOnAction(e -> view.showMenu());
        btnMenu.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 20px;");
        content.getChildren().addAll(lbl, btnMenu); view.showOverlay(content);
    }

    public static void main(String[] args) {
        launch(args);
    }
}