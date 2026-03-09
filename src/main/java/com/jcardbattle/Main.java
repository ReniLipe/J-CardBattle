package com.jcardbattle;

import com.jcardbattle.dao.CardDAOImpl;
import com.jcardbattle.dao.DbInitializer;
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
    private boolean isMyTurn = false;
    private int myRoll = 0;

    @Override
    public void start(Stage primaryStage) {
        DbInitializer.initialize();
        view = new GameView();
        setupEventHandlers(primaryStage);

        // Partiamo con una finestra 16:10 (es. 1280x800)
        Scene scene = new Scene(view.getRoot(), 1280, 800);

        // VINCOLO 16:10 SOLO IN WINDOWED
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!primaryStage.isFullScreen()) {
                // Rapporto 16:10 -> 1.6
                primaryStage.setHeight(newVal.doubleValue() / 1.6);
            }
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            }
        });

        primaryStage.setTitle("J-CardBattle - Magic Engine");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupEventHandlers(Stage stage) {
        view.getBtnSinglePlayer().setOnAction(e -> avviaNuovaPartita());

        view.getBtnMultiPlayer().setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("127.0.0.1");
            dialog.setTitle("Connessione");
            dialog.setHeaderText("Inserisci IP:");
            dialog.showAndWait().ifPresent(input -> {
                String ip = input; int port = 12345;
                if (input.contains(":")) {
                    String[] parts = input.split(":"); ip = parts[0];
                    try { port = Integer.parseInt(parts[1]); } catch (Exception ex) {}
                }
                view.log("Connessione a " + ip + ":" + port + "...");
                netClient = new NetworkClient(ip, port, this::gestisciMessaggioServer);
                avviaNuovaPartita();
                netClient.sendMessage("Giocatore pronto!");
            });
        });

        view.getBtnExit().setOnAction(e -> { if(netClient != null) netClient.close(); Platform.exit(); System.exit(0); });
        view.getBtnPause().setOnAction(e -> mostraMenuPausa());
        view.getBtnMinusLife().setOnAction(e -> { if(partitaFinita) return; lifePoints--; view.updateLife(lifePoints); checkGameOver(); if (netClient != null) netClient.sendMessage("VITA:" + lifePoints); });
        view.getBtnPlusLife().setOnAction(e -> { if(partitaFinita) return; lifePoints++; view.updateLife(lifePoints); if (netClient != null) netClient.sendMessage("VITA:" + lifePoints); });
        view.getDeckVisual().setOnMouseClicked(e -> { if(!partitaFinita) { view.animateDeckClick(); pescaCarta(); }});
        view.getBtnShuffle().setOnAction(e -> { if(playerDeck != null && !partitaFinita) { playerDeck.shuffle(); view.log("Mazzo mescolato!"); view.animateDeckClick(); }});
        view.getBtnSearch().setOnAction(e -> { if(!partitaFinita) eseguiRicerca(); });
        view.getBtnNextPhase().setOnAction(e -> { if(!partitaFinita) avanzaFase(); });

        for (int i = 0; i < 6; i++) {
            final int colorIndex = i;
            view.getManaLabels()[i].setOnMouseClicked(e -> {
                if(partitaFinita) return;
                if (e.getButton() == MouseButton.PRIMARY) manaPool[colorIndex]++;
                else if (e.getButton() == MouseButton.SECONDARY && manaPool[colorIndex] > 0) manaPool[colorIndex]--;
                view.updateManaDisplay(manaPool);
                if (netClient != null) sendManaUpdate();
            });
        }

        enableDropZone(view.getHandZone());
        enableDropZone(view.getMyPlaymat()); // Solo tu puoi giocare sul tuo tavolo!
        enableDropZone(view.getPlayerGraveyard());
        enableDropZone(view.getPlayerExile());
        setupPileBrowser(view.getPlayerGraveyard()); setupPileBrowser(view.getPlayerExile());
        enableDeckDropZone(view.getDeckVisual());
    }

    private void setupPileBrowser(GamePile pile) {
        pile.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                view.mostraBrowserCarte(pile.getZoneName(), pile.getCards(), card -> {
                    pile.removeCard(card); addCardToZone(card, view.getHandZone()); view.log("Recuperata " + card.getName() + " da " + pile.getZoneName());
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
        partitaFinita = false; lifePoints = 20; turnCount = 1; view.updateLife(lifePoints); view.getSearchField().clear();
        view.getHandZone().getChildren().clear();
        view.getMyPlaymat().getChildren().clear();
        view.getOppPlaymat().getChildren().clear();
        view.getPlayerGraveyard().clear(); view.getPlayerExile().clear();
        for(int i=0; i<6; i++) manaPool[i] = 0; view.updateManaDisplay(manaPool);
        if (netClient != null) sendManaUpdate();
        view.clearOpponentHand(); view.updateOpponentLife(20);

        try { playerDeck = new Deck(new CardDAOImpl().loadDeck(1)); playerDeck.shuffle(); view.updateDeckCount(playerDeck.size()); }
        catch (Exception e) { playerDeck = new Deck(Collections.emptyList()); }
        if (playerDeck.size() > 0) for(int i=0; i<7; i++) pescaCarta();

        if (netClient != null) {
            isMyTurn = false; view.getBtnNextPhase().setDisable(false); currentPhase = Phase.UNTAP;
            myRoll = (int)(Math.random() * 1000); view.log("In attesa avversario... Dado lanciato: " + myRoll);
            netClient.sendMessage("ROLL_INIZIO:" + myRoll);
        } else {
            isMyTurn = true; currentPhase = Phase.MAIN1; view.getBtnNextPhase().setDisable(false);
            updatePhaseUI(); view.log("--- PARTITA INIZIATA (Locale) ---");
        } view.showGame();
    }

    private void avanzaFase() {
        switch (currentPhase) {
            case UNTAP: currentPhase = Phase.DRAW; performDrawStep(); break;
            case DRAW: currentPhase = Phase.MAIN1; break;
            case MAIN1: currentPhase = Phase.COMBAT; break;
            case COMBAT: currentPhase = Phase.MAIN2; break;
            case MAIN2: currentPhase = Phase.END; break;
            case END: passaTurnoAllAvversario(); return;
        } updatePhaseUI();
        if (netClient != null) netClient.sendMessage("FASE:" + currentPhase.name());
    }

    private void passaTurnoAllAvversario() {
        if (netClient == null) { passaTurnoLocale(); return; }
        isMyTurn = false; view.log("--- Fine Turno. ---"); netClient.sendMessage("IL_TUO_TURNO");
    }

    private void passaTurnoLocale() {
        turnCount++; currentPhase = Phase.UNTAP;
        view.getMyPlaymat().getChildren().forEach(n -> { n.setRotate(0); n.setOpacity(1.0); });
        view.log("--- Turno " + turnCount + " ---"); updatePhaseUI();
    }

    private void performDrawStep() { pescaCarta(); }
    private void updatePhaseUI() { view.highlightPhase(currentPhase.name().replace("MAIN", "MAIN ")); }
    private void pescaCarta() {
        if (playerDeck == null) return; Card c = playerDeck.draw();
        if (c != null) { addCardToZone(c, view.getHandZone()); view.updateDeckCount(playerDeck.size()); if (netClient != null) netClient.sendMessage("PESCA"); }
        else { view.log("Mazzo finito!"); view.getDeckVisual().setOpacity(0.5); }
    }
    private void eseguiRicerca() {
        String query = view.getSearchField().getText().trim(); if (query.isEmpty() || playerDeck == null) return; Card c = playerDeck.search(query);
        if (c != null) { addCardToZone(c, view.getHandZone()); view.getSearchField().clear(); view.updateDeckCount(playerDeck.size()); if (netClient != null) netClient.sendMessage("PESCA"); }
    }
    private void checkGameOver() { if (lifePoints <= 0 && !partitaFinita) { partitaFinita = true; view.mostraSchermataFinePartita("SCONFITTA", Color.RED); } }
    private void triggerVictory() { if (!partitaFinita) { partitaFinita = true; view.mostraSchermataFinePartita("VITTORIA!", Color.LIGHTGREEN); } }

    private void addCardToZone(Card card, Pane zone) {
        if (card == null) return;
        VBox cardNode = new CardUI(card).createCardNode(false);
        cardNode.setUserData(card);
        setupCardInteractions(cardNode);
        zone.getChildren().add(cardNode);
    }

    private void setupCardInteractions(VBox cardNode) {
        // --- QUANDO IL MOUSE ENTRA SULLA CARTA ---
        cardNode.setOnMouseEntered(e -> {
            Card c = (Card) cardNode.getUserData();
            view.setCardPreview(c); // Aggiorna l'anteprima in alto a sinistra

            // Ingrandisce e alza la carta SOLO se si trova nella tua Mano
            if (cardNode.getParent() == view.getHandZone()) {
                cardNode.setViewOrder(-100);
                cardNode.setTranslateY(-40);
                cardNode.setScaleX(1.2);
                cardNode.setScaleY(1.2);
            }
        });

        // --- QUANDO IL MOUSE ESCE DALLA CARTA ---
        cardNode.setOnMouseExited(e -> {
            view.setCardPreview(null); // Pulisce l'anteprima

            // Ripristina l'aspetto normale SOLO se si trova nella tua Mano
            if (cardNode.getParent() == view.getHandZone()) {
                cardNode.setViewOrder(0);
                cardNode.setTranslateY(0);
                cardNode.setScaleX(1.0);
                cardNode.setScaleY(1.0);
            }
            // Se è sul playmat (tavolo), non fa niente: resta piccola e ferma al 50%!
        });

        // --- CLICK DESTRO PER TAPPARE ---
        cardNode.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY && !partitaFinita) {
                if (cardNode.getParent() != view.getHandZone()) {
                    double newAngle = (cardNode.getRotate() == 0) ? 90 : 0; cardNode.setRotate(newAngle);
                    if (netClient != null) { Card c = (Card) cardNode.getUserData(); String zone = getZoneName((Pane)cardNode.getParent()); if(!zone.equals("HAND")) netClient.sendMessage("TAP:" + c.getName() + ":" + zone + ":" + newAngle); }
                }
            }
        });

        // --- INIZIO TRASCINAMENTO (DRAG & DROP) ---
        cardNode.setOnDragDetected(event -> {
            if (partitaFinita) return; draggedCard = cardNode; Dragboard db = cardNode.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent(); content.putString("card_move"); db.setContent(content);
            WritableImage snap = cardNode.snapshot(new SnapshotParameters(), null); db.setDragView(snap, snap.getWidth()/2, snap.getHeight()/2);
            cardNode.setVisible(false); event.consume();
        });

        // --- FINE TRASCINAMENTO ---
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

                    double dropX = 0; double dropY = 0;
                    if(zone == view.getMyPlaymat()) {
                        // SCALA PICCOLA per il tavolo (0.5 = 50% della grandezza)
                        dropX = Math.round(e.getX() - 50);
                        dropY = Math.round(e.getY() - 70);
                        draggedCard.setLayoutX(dropX);
                        draggedCard.setLayoutY(dropY);
                        draggedCard.setScaleX(0.5);
                        draggedCard.setScaleY(0.5);
                        zone.getChildren().add(draggedCard);
                    } else if(zone instanceof GamePile) {
                        ((GamePile)zone).addCard((Card)draggedCard.getUserData());
                    } else {
                        // Ripristina SCALA NORMALE se va in Mano
                        draggedCard.setLayoutX(0); draggedCard.setLayoutY(0);
                        draggedCard.setScaleX(1.0); draggedCard.setScaleY(1.0);
                        zone.getChildren().add(draggedCard);
                    }

                    draggedCard.setVisible(true); draggedCard.setTranslateY(0); draggedCard.setViewOrder(0); draggedCard.setRotate(0);

                    if (netClient != null) {
                        Card c = (Card) draggedCard.getUserData();
                        if (c != null) {
                            if (sourceZone.equals("HAND") && destZone.equals("PLAYMAT")) {
                                netClient.sendMessage("GIOCA:" + c.getName() + ":" + destZone + ":" + dropX + ":" + dropY);
                            }
                            else if (!sourceZone.equals("HAND") && destZone.equals("PLAYMAT")) {
                                netClient.sendMessage("SPOSTA:" + c.getName() + ":" + sourceZone + ":" + destZone + ":" + dropX + ":" + dropY);
                            }
                            else if (!sourceZone.equals("HAND") && destZone.equals("HAND")) {
                                netClient.sendMessage("MANO:" + c.getName() + ":" + sourceZone);
                            }
                            else if (!destZone.equals("PLAYMAT") && !destZone.equals("HAND")) {
                                netClient.sendMessage("SPOSTA:" + c.getName() + ":" + sourceZone + ":" + destZone);
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
                Pane sourceParent = (Pane) draggedCard.getParent(); String sourceZone = getZoneName(sourceParent);
                Card cardObject = (Card) draggedCard.getUserData();
                if (cardObject != null) {
                    sourceParent.getChildren().remove(draggedCard); playerDeck.add(cardObject); playerDeck.shuffle();
                    view.updateDeckCount(playerDeck.size()); view.log("♻ " + cardObject.getName() + " tornata nel mazzo."); view.animateDeckClick();
                    if (netClient != null) { if (sourceZone.equals("HAND")) netClient.sendMessage("RITORNO:" + cardObject.getName() + ":HAND"); else netClient.sendMessage("RITORNO:" + cardObject.getName() + ":" + sourceZone); }
                    e.setDropCompleted(true);
                }
            } e.consume();
        });
    }

    private String getZoneName(Pane pane) {
        if (pane == view.getHandZone()) return "HAND";
        if (pane == view.getMyPlaymat()) return "PLAYMAT";
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
    public static void main(String[] args) { launch(args); }

    private void gestisciMessaggioServer(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;

        if (msg.startsWith("ROLL_INIZIO:")) {
            try {
                int oppRoll = Integer.parseInt(msg.split(":")[1]);
                Platform.runLater(() -> view.log("Avversario ha tirato: " + oppRoll));
                if (this.myRoll > oppRoll) {
                    Platform.runLater(() -> { isMyTurn = true; view.getBtnNextPhase().setDisable(false); currentPhase = Phase.MAIN1; view.highlightPhase("MAIN 1"); view.log("HAI VINTO IL SORTEGGIO! Inizi tu."); });
                } else if (this.myRoll < oppRoll) {
                    Platform.runLater(() -> { isMyTurn = false; view.getBtnNextPhase().setDisable(false); view.log("L'avversario ha vinto il sorteggio e inizia."); });
                } else { Platform.runLater(() -> view.log("Pareggio ai dadi... Rilancia!")); }
            } catch (Exception e) {}
        }
        else if (msg.equals("IL_TUO_TURNO")) {
            Platform.runLater(() -> {
                isMyTurn = true; turnCount++; currentPhase = Phase.UNTAP; view.getBtnNextPhase().setDisable(false);
                view.getMyPlaymat().getChildren().forEach(n -> n.setRotate(0));
                view.log("--- È IL TUO TURNO! (Turno " + turnCount + ") ---"); view.highlightPhase("UNTAP");
                if(netClient != null) netClient.sendMessage("FASE:UNTAP");
            });
        }
        else if (msg.startsWith("VITA:")) { try { int vitaAvversario = Integer.parseInt(msg.split(":")[1]); view.updateOpponentLife(vitaAvversario); if (vitaAvversario <= 0) Platform.runLater(this::triggerVictory); } catch (Exception e) {} }
        else if (msg.startsWith("FASE:")) { String rawPhase = msg.split(":")[1]; String uiPhase = rawPhase.replace("MAIN", "MAIN "); Platform.runLater(() -> { view.log("Avversario in fase: " + uiPhase); view.highlightPhase(uiPhase); if (rawPhase.equals("UNTAP")) view.untapAllOpponentCards(); }); }
        else if (msg.startsWith("MANA:")) { try { String[] parts = msg.split(":")[1].split(","); int[] oppMana = new int[6]; for(int i=0; i<6; i++) oppMana[i] = Integer.parseInt(parts[i]); view.updateOpponentManaDisplay(oppMana); } catch (Exception e) {} }
        else if (msg.startsWith("TAP:")) { try { String[] parts = msg.split(":"); view.rotateOpponentCard(parts[1], Double.parseDouble(parts[3])); } catch (Exception e) {} }
        else if (msg.contains("Giocatore connesso") || msg.contains("Giocatore pronto")) { Platform.runLater(() -> { view.log("[SISTEMA]: Connessione stabilita."); if (netClient != null) { netClient.sendMessage("HAND_SIZE:" + view.getHandZone().getChildren().size()); netClient.sendMessage("VITA:" + lifePoints); sendManaUpdate(); } }); }
        else if (msg.startsWith("HAND_SIZE:")) { try { view.setOpponentHandSize(Integer.parseInt(msg.split(":")[1])); } catch(Exception e){} }
        else if (msg.equals("PESCA")) { view.addOpponentHandCard(); }
        else if (msg.startsWith("GIOCA:")) {
            try {
                String[] parts = msg.split(":"); String name = parts[1]; String dest = parts[2]; view.log("Gioca: " + name);
                if (dest.equals("PLAYMAT") && parts.length >= 5) {
                    double x = Double.parseDouble(parts[3]); double y = Double.parseDouble(parts[4]);
                    view.addOpponentCard(name, x, y);
                } else if(dest.equals("GRAVEYARD") || dest.equals("EXILE")) { view.addCardToOpponentPile(dest, name); }
                view.removeOpponentHandCard();
            } catch (Exception e) { e.printStackTrace(); }
        }
        else if (msg.startsWith("SPOSTA:")) {
            try {
                String[] parts = msg.split(":"); String name = parts[1]; String src = parts[2]; String dest = parts[3];
                if(src.equals("GRAVEYARD") || src.equals("EXILE")) view.removeCardFromOpponentPile(src);
                else view.removeOpponentCard(name);

                if (dest.equals("PLAYMAT") && parts.length >= 6) {
                    double x = Double.parseDouble(parts[4]); double y = Double.parseDouble(parts[5]);
                    view.addOpponentCard(name, x, y);
                } else if(dest.equals("GRAVEYARD") || dest.equals("EXILE")) { view.addCardToOpponentPile(dest, name); }
            } catch (Exception e) { e.printStackTrace(); }
        }
        else if (msg.startsWith("MANO:")) { try { String[] parts = msg.split(":"); String name = parts[1]; String src = parts[2]; view.log("Riprende in mano: " + name); if(src.equals("GRAVEYARD") || src.equals("EXILE")) view.removeCardFromOpponentPile(src); else view.removeOpponentCard(name); view.addOpponentHandCard(); } catch (Exception e) {} }
        else if (msg.startsWith("RITORNO:")) { try { String[] parts = msg.split(":"); String name = parts[1]; String src = parts[2]; view.log("Torna nel mazzo: " + name); if (src.equals("HAND")) view.removeOpponentHandCard(); else if(src.equals("GRAVEYARD") || src.equals("EXILE")) view.removeCardFromOpponentPile(src); else view.removeOpponentCard(name); } catch (Exception e) {} }
        else { Platform.runLater(() -> view.log("[OPP]: " + msg)); }
    }
}