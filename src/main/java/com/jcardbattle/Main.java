package com.jcardbattle;

import com.jcardbattle.dao.CardDAOImpl;
import com.jcardbattle.model.Card;
import com.jcardbattle.model.Deck;
import com.jcardbattle.view.CardUI;
import com.jcardbattle.view.GamePile;
import com.jcardbattle.view.GameView;
import com.jcardbattle.network.NetworkClient;

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
    private static Node draggedCard;

    private int lifePoints = 20;
    private Deck playerDeck;

    private enum Phase { UNTAP, DRAW, MAIN1, COMBAT, MAIN2, END }
    private Phase currentPhase = Phase.MAIN1;
    private int turnCount = 1;

    private int[] manaPool = {0, 0, 0, 0, 0, 0};

    private NetworkClient netClient;
    private boolean partitaFinita = false;

    @Override
    public void start(Stage primaryStage) {
        view = new GameView();
        setupEventHandlers(primaryStage);
        Scene scene = new Scene(view.getRoot(), 1024, 768);
        scene.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.F) primaryStage.setFullScreen(!primaryStage.isFullScreen()); });
        primaryStage.setTitle("J-CardBattle - Magic Engine");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void setupEventHandlers(Stage stage) {
        view.getBtnSinglePlayer().setOnAction(e -> avviaNuovaPartita());

        view.getBtnMultiPlayer().setOnAction(e -> {
            view.log("Connessione al server...");
            netClient = new NetworkClient("127.0.0.1", 12345, msg -> gestisciMessaggioServer(msg));
            avviaNuovaPartita();
            netClient.sendMessage("Giocatore pronto!");
        });

        view.getBtnExit().setOnAction(e -> { Platform.exit(); System.exit(0); });
        view.getBtnPause().setOnAction(e -> mostraMenuPausa());

        view.getBtnMinusLife().setOnAction(e -> {
            if(partitaFinita) return;
            lifePoints--; view.updateLife(lifePoints); checkGameOver();
            if (netClient != null) netClient.sendMessage("VITA:" + lifePoints);
        });
        view.getBtnPlusLife().setOnAction(e -> {
            if(partitaFinita) return;
            lifePoints++; view.updateLife(lifePoints);
            if (netClient != null) netClient.sendMessage("VITA:" + lifePoints);
        });

        view.getDeckVisual().setOnMouseClicked(e -> { if(!partitaFinita) { view.animateDeckClick(); pescaCarta(); }});
        view.getBtnShuffle().setOnAction(e -> { if(playerDeck != null && !partitaFinita) { playerDeck.shuffle(); view.log("Mazzo mescolato!"); view.animateDeckClick(); }});
        view.getBtnSearch().setOnAction(e -> { if(!partitaFinita) eseguiRicerca(); });
        view.getBtnNextPhase().setOnAction(e -> { if(!partitaFinita) avanzaFase(); });

        Label[] manaLabels = view.getManaLabels();
        for (int i = 0; i < 6; i++) {
            final int colorIndex = i;
            manaLabels[i].setOnMouseClicked(e -> {
                if(partitaFinita) return;
                if (e.getButton() == MouseButton.PRIMARY) manaPool[colorIndex]++;
                else if (e.getButton() == MouseButton.SECONDARY && manaPool[colorIndex] > 0) manaPool[colorIndex]--;
                view.updateManaDisplay(manaPool);
                if (netClient != null) sendManaUpdate();
            });
        }

        enableDropZone(view.getHandZone());
        enableDropZone(view.getCombatRow());
        enableDropZone(view.getLandRow());
        enableDropZone(view.getPlayerGraveyard());
        enableDropZone(view.getPlayerExile());

        setupPileBrowser(view.getPlayerGraveyard());
        setupPileBrowser(view.getPlayerExile());

        enableDeckDropZone(view.getDeckVisual());
    }

    private void setupPileBrowser(GamePile pile) {
        pile.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                view.mostraBrowserCarte(pile.getZoneName(), pile.getCards(), card -> {
                    pile.removeCard(card);
                    addCardToZone(card, view.getHandZone());
                    view.log("Recuperata " + card.getName() + " da " + pile.getZoneName());
                    if (netClient != null) netClient.sendMessage("MANO:" + card.getName() + ":" + pile.getZoneName());
                });
            }
        });
    }

    private void sendManaUpdate() {
        StringBuilder sb = new StringBuilder("MANA:");
        for(int i=0; i<6; i++) sb.append(manaPool[i]).append(i<5 ? "," : "");
        netClient.sendMessage(sb.toString());
    }

    private void avviaNuovaPartita() {
        partitaFinita = false; lifePoints = 20; turnCount = 1;
        view.updateLife(lifePoints); view.getSearchField().clear();
        view.getHandZone().getChildren().clear(); view.getCombatRow().getChildren().clear(); view.getLandRow().getChildren().clear();
        view.getPlayerGraveyard().clear(); view.getPlayerExile().clear();

        for(int i=0; i<6; i++) manaPool[i] = 0; view.updateManaDisplay(manaPool);
        if (netClient != null) sendManaUpdate();

        view.clearOpponentHand();
        view.updateOpponentLife(20);

        try {
            CardDAOImpl dao = new CardDAOImpl();
            playerDeck = new Deck(dao.loadDeck(1));
            playerDeck.shuffle(); view.updateDeckCount(playerDeck.size());
        } catch (Exception e) {
            playerDeck = new Deck(Collections.emptyList());
        }
        if (playerDeck.size() > 0) for(int i=0; i<7; i++) pescaCarta();

        currentPhase = Phase.MAIN1; updatePhaseUI(); view.showGame();
        view.log("--- PARTITA INIZIATA ---");
    }

    private void avanzaFase() {
        switch (currentPhase) {
            case UNTAP: currentPhase = Phase.DRAW; performDrawStep(); break;
            case DRAW: currentPhase = Phase.MAIN1; break;
            case MAIN1: currentPhase = Phase.COMBAT; break;
            case COMBAT: currentPhase = Phase.MAIN2; break;
            case MAIN2: currentPhase = Phase.END; break;
            case END: passaTurno(); break;
        }
        updatePhaseUI();
        if (netClient != null) netClient.sendMessage("FASE:" + currentPhase.name());
    }

    private void passaTurno() {
        turnCount++; currentPhase = Phase.UNTAP;
        view.getLandRow().getChildren().forEach(n -> { n.setRotate(0); n.setOpacity(1.0); });
        view.getCombatRow().getChildren().forEach(n -> { n.setRotate(0); n.setOpacity(1.0); });
        view.log("--- Turno " + turnCount + " ---");
    }
    private void performDrawStep() { pescaCarta(); }
    private void updatePhaseUI() { view.highlightPhase(currentPhase.name().replace("MAIN", "MAIN ")); }

    private void pescaCarta() {
        if (playerDeck == null) return;
        Card c = playerDeck.draw();
        if (c != null) {
            addCardToZone(c, view.getHandZone()); view.updateDeckCount(playerDeck.size());
            if (netClient != null) netClient.sendMessage("PESCA");
        } else {
            view.log("Mazzo finito!"); view.getDeckVisual().setOpacity(0.5);
        }
    }

    private void eseguiRicerca() {
        String query = view.getSearchField().getText().trim();
        if (query.isEmpty() || playerDeck == null) return;
        Card c = playerDeck.search(query);
        if (c != null) {
            addCardToZone(c, view.getHandZone()); view.getSearchField().clear(); view.updateDeckCount(playerDeck.size());
            if (netClient != null) netClient.sendMessage("PESCA");
        }
    }

    private void checkGameOver() {
        if (lifePoints <= 0 && !partitaFinita) {
            partitaFinita = true; view.mostraSchermataFinePartita("SCONFITTA", Color.RED);
        }
    }
    private void triggerVictory() {
        if (!partitaFinita) { partitaFinita = true; view.mostraSchermataFinePartita("VITTORIA!", Color.LIGHTGREEN); }
    }

    private void addCardToZone(Card card, Pane zone) {
        if (card == null) return;
        VBox cardNode = new CardUI(card).createCardNode(); cardNode.setUserData(card);
        setupCardInteractions(cardNode); zone.getChildren().add(cardNode);
    }

    private void setupCardInteractions(VBox cardNode) {
        cardNode.setOnMouseEntered(e -> { if (cardNode.getRotate() == 0) { cardNode.setViewOrder(-1); cardNode.setTranslateY(-40); }});
        cardNode.setOnMouseExited(e -> { cardNode.setViewOrder(0); cardNode.setTranslateY(0); });

        cardNode.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY && !partitaFinita) {
                if (cardNode.getParent() != view.getHandZone()) {
                    double newAngle = (cardNode.getRotate() == 0) ? 90 : 0;
                    cardNode.setRotate(newAngle);
                    if (netClient != null) {
                        Card c = (Card) cardNode.getUserData();
                        String zone = getZoneName((Pane)cardNode.getParent());
                        if(!zone.equals("HAND")) netClient.sendMessage("TAP:" + c.getName() + ":" + zone + ":" + newAngle);
                    }
                }
            }
        });

        cardNode.setOnDragDetected(event -> {
            if (cardNode.getRotate() != 0 || partitaFinita) return;
            draggedCard = cardNode;
            Dragboard db = cardNode.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent(); content.putString("card_move"); db.setContent(content);
            WritableImage snap = cardNode.snapshot(new SnapshotParameters(), null);
            db.setDragView(snap, snap.getWidth()/2, snap.getHeight()/2);
            cardNode.setVisible(false); event.consume();
        });
        cardNode.setOnDragDone(event -> { if (event.getTransferMode() != TransferMode.MOVE) cardNode.setVisible(true); event.consume(); });
    }

    private void enableDropZone(Pane zone) {
        zone.setOnDragOver(e -> { if (e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.MOVE); e.consume(); });
        zone.setOnDragDropped(e -> {
            if (draggedCard != null && !partitaFinita) {
                Pane sourceParent = (Pane) draggedCard.getParent();
                String sourceZone = getZoneName(sourceParent);
                String destZone = getZoneName(zone);

                if(sourceParent.getChildren().contains(draggedCard)) {
                    sourceParent.getChildren().remove(draggedCard);

                    if(zone instanceof GamePile) {
                        Card c = (Card) draggedCard.getUserData();
                        ((GamePile)zone).addCard(c);
                    } else {
                        zone.getChildren().add(draggedCard);
                    }

                    draggedCard.setVisible(true); draggedCard.setTranslateY(0); draggedCard.setViewOrder(0); draggedCard.setRotate(0);

                    if (netClient != null) {
                        Card c = (Card) draggedCard.getUserData();
                        if (c != null) {
                            if (sourceZone.equals("HAND") && !destZone.equals("HAND")) {
                                netClient.sendMessage("GIOCA:" + c.getName() + ":" + destZone);
                            }
                            else if (!sourceZone.equals("HAND") && !destZone.equals("HAND")) {
                                netClient.sendMessage("SPOSTA:" + c.getName() + ":" + sourceZone + ":" + destZone);
                            }
                            else if (!sourceZone.equals("HAND") && destZone.equals("HAND")) {
                                netClient.sendMessage("MANO:" + c.getName() + ":" + sourceZone);
                            }
                        }
                    }
                }
                e.setDropCompleted(true);
            } e.consume();
        });
    }

    private void enableDeckDropZone(StackPane deckVisual) {
        deckVisual.setOnDragOver(e -> { if (e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.MOVE); e.consume(); });
        deckVisual.setOnDragDropped(e -> {
            if (draggedCard != null && !partitaFinita) {
                Pane sourceParent = (Pane) draggedCard.getParent();
                String sourceZone = getZoneName(sourceParent);

                Card cardObject = (Card) draggedCard.getUserData();
                if (cardObject != null) {
                    sourceParent.getChildren().remove(draggedCard);
                    playerDeck.add(cardObject); playerDeck.shuffle();
                    view.updateDeckCount(playerDeck.size()); view.log("♻ " + cardObject.getName() + " tornata nel mazzo."); view.animateDeckClick();

                    if (netClient != null) {
                        if (sourceZone.equals("HAND")) netClient.sendMessage("RITORNO:" + cardObject.getName() + ":HAND");
                        else netClient.sendMessage("RITORNO:" + cardObject.getName() + ":" + sourceZone);
                    }
                    e.setDropCompleted(true);
                }
            } e.consume();
        });
    }

    private String getZoneName(Pane pane) {
        if (pane == view.getHandZone()) return "HAND";
        if (pane == view.getCombatRow()) return "COMBAT";
        if (pane == view.getLandRow()) return "LAND";
        if (pane == view.getPlayerGraveyard()) return "GRAVEYARD";
        if (pane == view.getPlayerExile()) return "EXILE";
        return "UNKNOWN";
    }

    private void mostraMenuPausa() {
        VBox content = new VBox(20); content.setAlignment(Pos.CENTER);
        Label lbl = new Label("PAUSA"); lbl.setFont(Font.font("Impact", 60)); lbl.setTextFill(Color.WHITE);
        Button btnResume = new Button("▶ RIPRENDI"); btnResume.setOnAction(e -> view.hideOverlay());
        Button btnMenu = new Button("🏠 MENU"); btnMenu.setOnAction(e -> view.showMenu());
        btnResume.setStyle("-fx-background-color:#27ae60; -fx-text-fill:white;"); btnMenu.setStyle("-fx-background-color:#e67e22; -fx-text-fill:white;");
        content.getChildren().addAll(lbl, btnResume, btnMenu); view.showOverlay(content);
    }
    private void mostraOverlayMessaggio(String testo, boolean resume) { view.mostraSchermataFinePartita(testo, Color.WHITE); }
    public static void main(String[] args) { launch(args); }

    private void gestisciMessaggioServer(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;

        if (msg.startsWith("VITA:")) {
            try {
                int vitaAvversario = Integer.parseInt(msg.split(":")[1]);
                view.updateOpponentLife(vitaAvversario);
                if (vitaAvversario <= 0) Platform.runLater(this::triggerVictory);
            } catch (Exception e) {}
        }
        else if (msg.startsWith("FASE:")) {
            String rawPhase = msg.split(":")[1];
            String uiPhase = rawPhase.replace("MAIN", "MAIN ");
            Platform.runLater(() -> {
                view.log("Fase Avversario: " + uiPhase);
                view.highlightPhase(uiPhase);
                if (rawPhase.equals("UNTAP")) view.untapAllOpponentCards();
            });
        }
        else if (msg.startsWith("MANA:")) {
            try {
                String[] parts = msg.split(":")[1].split(",");
                int[] oppMana = new int[6];
                for(int i=0; i<6; i++) oppMana[i] = Integer.parseInt(parts[i]);
                view.updateOpponentManaDisplay(oppMana);
            } catch (Exception e) {}
        }
        else if (msg.startsWith("TAP:")) {
            try {
                String[] parts = msg.split(":");
                view.rotateOpponentCard(parts[1], parts[2], Double.parseDouble(parts[3]));
            } catch (Exception e) {}
        }
        else if (msg.contains("Giocatore connesso") || msg.contains("Giocatore pronto")) {
            Platform.runLater(() -> {
                view.log("[SISTEMA]: Avversario connesso.");
                if (netClient != null) {
                    int myHandSize = view.getHandZone().getChildren().size();
                    netClient.sendMessage("HAND_SIZE:" + myHandSize);
                    netClient.sendMessage("VITA:" + lifePoints);
                    sendManaUpdate();
                }
            });
        }
        else if (msg.startsWith("HAND_SIZE:")) {
            try { view.setOpponentHandSize(Integer.parseInt(msg.split(":")[1])); } catch(Exception e){}
        }
        else if (msg.equals("PESCA")) {
            view.addOpponentHandCard();
        }

        else if (msg.startsWith("GIOCA:")) {
            try {
                String[] parts = msg.split(":");
                String name = parts[1];
                String dest = parts[2];
                view.log("Gioca: " + name);

                if(dest.equals("GRAVEYARD") || dest.equals("EXILE")) view.addCardToOpponentPile(dest, name);
                else view.addOpponentCard(name, dest);

                view.removeOpponentHandCard();
            } catch (Exception e) {}
        }
        else if (msg.startsWith("SPOSTA:")) {
            try {
                String[] parts = msg.split(":");
                String name = parts[1];
                String src = parts[2];
                String dest = parts[3];

                if(src.equals("GRAVEYARD") || src.equals("EXILE")) view.removeCardFromOpponentPile(src);
                else view.removeOpponentCard(name, src);

                if(dest.equals("GRAVEYARD") || dest.equals("EXILE")) view.addCardToOpponentPile(dest, name);
                else view.addOpponentCard(name, dest);
            } catch (Exception e) {}
        }
        else if (msg.startsWith("MANO:")) {
            try {
                String[] parts = msg.split(":");
                String name = parts[1];
                String src = parts[2];
                view.log("Riprende in mano: " + name);

                if(src.equals("GRAVEYARD") || src.equals("EXILE")) view.removeCardFromOpponentPile(src);
                else view.removeOpponentCard(name, src);

                view.addOpponentHandCard();
            } catch (Exception e) {}
        }
        else if (msg.startsWith("RITORNO:")) {
            try {
                String[] parts = msg.split(":");
                String name = parts[1];
                String src = parts[2];
                view.log("Torna nel mazzo: " + name);

                if (src.equals("HAND")) view.removeOpponentHandCard();
                else if(src.equals("GRAVEYARD") || src.equals("EXILE")) view.removeCardFromOpponentPile(src);
                else view.removeOpponentCard(name, src);
            } catch (Exception e) {}
        }
        else {
            Platform.runLater(() -> view.log("[OPP]: " + msg));
        }
    }
}