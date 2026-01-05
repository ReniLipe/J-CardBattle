package com.jcardbattle;

import com.jcardbattle.dao.CardDAOImpl;
import com.jcardbattle.model.Card;
import com.jcardbattle.model.Deck;
import com.jcardbattle.view.CardUI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    // --- VARIABILI GLOBALI ---
    private static Node draggedCard;

    // Logica di Gioco
    private int lifePoints = 30;
    private Deck playerDeck;

    // --- UI ELEMENTS (LAYERS) ---
    private StackPane rootLayout;
    private BorderPane gameLayer;
    private VBox mainMenuLayer;
    private VBox overlayMenuLayer;

    // Elementi aggiornabili
    private Label lifeLabel;
    private TextArea gameLog;
    private Label deckCountLabel;
    private StackPane deckVisual;
    private TextField searchField;

    // ZONE DEL TAVOLO (Riferimenti diretti per evitare che spariscano)
    private HBox handZone;
    private HBox combatRow;    // CORREZIONE: Salviamo la riga specifica
    private HBox landRow;      // CORREZIONE: Salviamo la riga specifica

    @Override
    public void start(Stage primaryStage) {
        rootLayout = new StackPane();
        rootLayout.setStyle("-fx-background-color: #2c3e50;");

        createGameLayer(primaryStage);
        createMainMenuLayer();
        createOverlayLayer();

        rootLayout.getChildren().addAll(gameLayer, mainMenuLayer, overlayMenuLayer);

        Scene scene = new Scene(rootLayout, 1024, 768);
        setupFullScreenKeys(primaryStage, scene);

        primaryStage.setTitle("J-CardBattle");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    // =================================================================
    // LAYER 1: IL GIOCO
    // =================================================================
    private void createGameLayer(Stage stage) {
        gameLayer = new BorderPane();
        gameLayer.setStyle("-fx-background-color: #2c3e50;");
        gameLayer.setVisible(false);

        // --- TOP BAR ---
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #7f8c8d; -fx-border-width: 0 0 2 0;");

        Button btnMenu = new Button("⚙ PAUSA");
        btnMenu.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnMenu.setOnAction(e -> mostraOverlayPausa("PAUSA GIOCO", true));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnMinus = new Button("-");
        Button btnPlus = new Button("+");
        String btnStyle = "-fx-background-radius: 50; -fx-min-width: 40px; -fx-min-height: 40px; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-cursor: hand;";
        btnMinus.setStyle(btnStyle + "-fx-background-color: #e74c3c;");
        btnPlus.setStyle(btnStyle + "-fx-background-color: #27ae60;");

        lifeLabel = new Label("VITA: 30");
        lifeLabel.setTextFill(Color.WHITE);
        lifeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        btnMinus.setOnAction(e -> { lifePoints--; aggiornaVita(); });
        btnPlus.setOnAction(e -> { lifePoints++; aggiornaVita(); });

        topBar.getChildren().addAll(btnMenu, spacer, btnMinus, lifeLabel, btnPlus, new Region());
        gameLayer.setTop(topBar);

        // --- CENTER BOARD ---
        VBox boardContainer = new VBox(20);
        boardContainer.setPadding(new Insets(20));
        boardContainer.setAlignment(Pos.CENTER);

        // CORREZIONE FONDAMENTALE: Creiamo le HBox qui e le passiamo al wrapper
        combatRow = new HBox(15);
        landRow = new HBox(15);

        // Usiamo un metodo helper per dare stile e creare il titolo (Wrapper)
        VBox combatWrapper = setupZoneWrapper(combatRow, "⚔ ZONA COMBATTIMENTO", "#e74c3c");
        VBox landWrapper = setupZoneWrapper(landRow, "🌲 ZONA TERRE", "#27ae60");

        boardContainer.getChildren().addAll(combatWrapper, landWrapper);
        gameLayer.setCenter(boardContainer);

        // --- BOTTOM HAND ---
        handZone = new HBox(-50);
        handZone.setPadding(new Insets(20, 20, 50, 80));
        handZone.setPrefHeight(280);
        handZone.setAlignment(Pos.BOTTOM_CENTER);
        handZone.setStyle("-fx-background-color: #34495e; -fx-border-color: #1abc9c; -fx-border-width: 4 0 0 0;");
        enableDropZone(handZone);
        gameLayer.setBottom(handZone);

        // --- RIGHT PANEL ---
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(280);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #1abc9c; -fx-border-width: 0 0 0 2;");

        // Mazzo Grafico
        deckVisual = new StackPane();
        deckVisual.setPrefSize(140, 200); deckVisual.setMaxSize(140, 200);
        deckVisual.setStyle("-fx-background-color: linear-gradient(to bottom right, #2980b9, #2c3e50); -fx-border-color: white; -fx-border-width: 5; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 5); -fx-cursor: hand;");

        Label deckLogo = new Label("J");
        deckLogo.setFont(Font.font("Times New Roman", FontWeight.BOLD, 80));
        deckLogo.setTextFill(Color.rgb(255, 255, 255, 0.2));

        deckCountLabel = new Label("0");
        deckCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        deckCountLabel.setTextFill(Color.WHITE);
        deckCountLabel.setStyle("-fx-background-color: black; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
        StackPane.setAlignment(deckCountLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(deckCountLabel, new Insets(10));

        deckVisual.getChildren().addAll(deckLogo, deckCountLabel);
        deckVisual.setOnMouseClicked(e -> {
            deckVisual.setTranslateY(4);
            new Timer().schedule(new TimerTask() { @Override public void run() { Platform.runLater(() -> deckVisual.setTranslateY(0)); }}, 100);
            pescaCarta(handZone);
        });

        // Ricerca
        searchField = new TextField(); searchField.setPromptText("Cerca carta...");
        Button searchBtn = new Button("🔍 CERCA");
        searchBtn.setMaxWidth(Double.MAX_VALUE);
        searchBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> eseguiRicerca(handZone));

        gameLog = new TextArea(); gameLog.setEditable(false); gameLog.setWrapText(true); gameLog.setPrefHeight(200);
        gameLog.setStyle("-fx-control-inner-background: #34495e; -fx-text-fill: white;");

        rightPanel.getChildren().addAll(new Label("MAZZO"), deckVisual, new Separator(), new Label("RICERCA"), searchField, searchBtn, new Separator(), new Label("LOG"), gameLog);
        gameLayer.setRight(rightPanel);
    }

    // =================================================================
    // LAYER 2: MENU PRINCIPALE
    // =================================================================
    private void createMainMenuLayer() {
        mainMenuLayer = new VBox(30);
        mainMenuLayer.setAlignment(Pos.CENTER);
        mainMenuLayer.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c3e50, #000000);");

        Label title = new Label("J-CARDBATTLE");
        title.setFont(Font.font("Impact", 80));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(20, Color.BLACK));
        Label subtitle = new Label("No Popups Edition");
        subtitle.setFont(Font.font("Arial", 20)); subtitle.setTextFill(Color.LIGHTGRAY);

        Button btnPlay = createStyledButton("⚔ NUOVA PARTITA", "#e67e22");
        btnPlay.setOnAction(e -> avviaNuovaPartita());

        Button btnExit = createStyledButton("❌ ESCI", "#c0392b");
        btnExit.setOnAction(e -> { Platform.exit(); System.exit(0); });

        mainMenuLayer.getChildren().addAll(title, subtitle, new Separator(), btnPlay, btnExit);
    }

    // =================================================================
    // LAYER 3: OVERLAY
    // =================================================================
    private void createOverlayLayer() {
        overlayMenuLayer = new VBox(20);
        overlayMenuLayer.setAlignment(Pos.CENTER);
        overlayMenuLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        overlayMenuLayer.setVisible(false);
    }

    // =================================================================
    // LOGICA GIOCO
    // =================================================================

    private void avviaNuovaPartita() {
        // 1. Reset Dati
        lifePoints = 30;
        lifeLabel.setText("VITA: 30");
        lifeLabel.setTextFill(Color.WHITE);
        gameLog.clear();
        searchField.clear();

        // CORREZIONE: Pulisco solo i figli delle HBox, NON le HBox stesse
        handZone.getChildren().clear();
        combatRow.getChildren().clear(); // Ora sicuro
        landRow.getChildren().clear();   // Ora sicuro

        log("Caricamento partita...");

        // 2. Carica Mazzo
        try {
            CardDAOImpl dao = new CardDAOImpl();
            playerDeck = new Deck(dao.loadDeck(1));
            if (playerDeck.size() > 0) playerDeck.shuffle();
            deckCountLabel.setText(playerDeck.size()+"");
        } catch (Exception e) {
            log("Errore DB: " + e.getMessage());
            playerDeck = new Deck(Collections.emptyList());
        }

        // 3. Pesca iniziale
        if (playerDeck.size() > 0) {
            for(int i=0; i<3; i++) pescaCarta(handZone);
        }

        mainMenuLayer.setVisible(false);
        overlayMenuLayer.setVisible(false);
        gameLayer.setVisible(true);
    }

    private void mostraOverlayPausa(String titolo, boolean mostraTastoRiprendi) {
        overlayMenuLayer.getChildren().clear();

        Label lblTitle = new Label(titolo);
        lblTitle.setFont(Font.font("Impact", 60));
        lblTitle.setTextFill(Color.WHITE);

        Button btnResume = createStyledButton("▶ RIPRENDI", "#27ae60");
        btnResume.setOnAction(e -> overlayMenuLayer.setVisible(false));

        Button btnMenu = createStyledButton("🏠 TORNA AL MENU", "#e67e22");
        btnMenu.setOnAction(e -> {
            overlayMenuLayer.setVisible(false);
            gameLayer.setVisible(false);
            mainMenuLayer.setVisible(true);
        });

        Button btnExit = createStyledButton("❌ ESCI DAL GIOCO", "#c0392b");
        btnExit.setOnAction(e -> Platform.exit());

        overlayMenuLayer.getChildren().add(lblTitle);
        if (mostraTastoRiprendi) overlayMenuLayer.getChildren().add(btnResume);
        overlayMenuLayer.getChildren().addAll(btnMenu, btnExit);

        overlayMenuLayer.setVisible(true);
    }

    private void aggiornaVita() {
        lifeLabel.setText("VITA: " + lifePoints);
        if (lifePoints <= 10) lifeLabel.setTextFill(Color.RED);
        else lifeLabel.setTextFill(Color.WHITE);
        if (lifePoints <= 0) mostraOverlayPausa("GAME OVER", false);
    }

    // =================================================================
    // UTILITIES
    // =================================================================

    // CORREZIONE: Metodo Helper per decorare le righe (Wraps HBox in VBox)
    private VBox setupZoneWrapper(HBox row, String title, String colorHex) {
        // Configura la riga (che è passata come argomento)
        row.setPrefHeight(200);
        row.setPrefWidth(900);
        row.setAlignment(Pos.CENTER);
        row.setStyle("-fx-border-color: " + colorHex + "; -fx-border-width: 2; -fx-background-color: " + colorHex + "1A; -fx-background-radius: 10; -fx-border-radius: 10;");

        enableDropZone(row); // Rende la riga interattiva

        Label label = new Label(title);
        label.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        VBox wrapper = new VBox(5, label, row);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(300); btn.setPrefHeight(60);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
        return btn;
    }

    private void pescaCarta(Pane handZone) {
        Card c = playerDeck.draw();
        if (c != null) {
            addCardToHand(c, handZone);
            log("Pescato: " + c.getName());
            deckCountLabel.setText(playerDeck.size()+"");
        } else {
            log("Mazzo finito!");
            deckVisual.setOpacity(0.5);
        }
    }

    private void eseguiRicerca(Pane handZone) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;
        Card c = playerDeck.search(query);
        if (c != null) {
            addCardToHand(c, handZone);
            log("Trovato: " + c.getName());
            searchField.clear();
            deckCountLabel.setText(playerDeck.size()+"");
        } else {
            log("Non trovato: " + query);
        }
    }

    private void log(String msg) {
        gameLog.appendText("> " + msg + "\n");
        gameLog.setScrollTop(Double.MAX_VALUE);
    }

    private void addCardToHand(Card card, Pane handZone) {
        if (card == null) return;
        VBox cardNode = new CardUI(card).createCardNode();
        setupCardInteractions(cardNode, card);
        handZone.getChildren().add(cardNode);
    }

    private void setupFullScreenKeys(Stage stage, Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F) stage.setFullScreen(!stage.isFullScreen());
        });
    }

    // --- INTERAZIONI STANDARD ---
    private void setupCardInteractions(VBox cardNode, Card card) {
        cardNode.setOnMouseEntered(e -> {
            if (cardNode.getRotate() == 0) { cardNode.setViewOrder(-1); cardNode.setTranslateY(-40); }
        });
        cardNode.setOnMouseExited(e -> { cardNode.setViewOrder(0); cardNode.setTranslateY(0); });
        cardNode.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                if (cardNode.getParent() == handZone) return;
                cardNode.setRotate(cardNode.getRotate() == 0 ? 90 : 0);
            }
        });
        cardNode.setOnDragDetected(event -> {
            if (cardNode.getRotate() != 0) return;
            draggedCard = cardNode;
            Dragboard db = cardNode.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent(); content.putString("c"); db.setContent(content);
            WritableImage snap = cardNode.snapshot(new SnapshotParameters(), null);
            db.setDragView(snap, snap.getWidth()/2, snap.getHeight()/2);
            cardNode.setVisible(false); event.consume();
        });
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

    public static void main(String[] args) {
        launch(args);
    }
}