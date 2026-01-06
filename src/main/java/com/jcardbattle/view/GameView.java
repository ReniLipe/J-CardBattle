package com.jcardbattle.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GameView {

    private StackPane rootLayout;
    private BorderPane gameLayer;
    private VBox mainMenuLayer;
    private VBox overlayMenuLayer;

    // --- ZONE TAVOLO ---
    private HBox handZone;
    private HBox combatRow;
    private HBox landRow;
    private HBox opponentHand, opponentCombatRow, opponentLandRow;

    // --- UI INFORMATIVA ---
    private Label lifeLabel;
    private Label opponentLifeLabel;

    // SISTEMA MANA INTERATTIVO (6 etichette per i colori)
    private Label[] manaLabels = new Label[6];

    // SISTEMA FASI
    private HBox phaseBar;
    private Map<String, Label> phaseLabels = new HashMap<>();
    private Button btnNextPhase;

    // Pannello Destro
    private TextArea gameLog;
    private Label deckCountLabel;
    private StackPane deckVisual;
    private TextField searchField;

    // Bottoni
    private Button btnSinglePlayer, btnMultiPlayer, btnExit, btnSearch, btnPause, btnMinusLife, btnPlusLife, btnShuffle;

    public GameView() {
        initUI();
    }

    private void initUI() {
        rootLayout = new StackPane();
        rootLayout.setStyle("-fx-background-color: #2c3e50;");

        createGameLayer();
        createMainMenuLayer(); // <--- QUI HO SISTEMATO IL CODICE
        createOverlayLayer();

        // Ordine: Gioco sotto, Menu sopra, Overlay in cima
        rootLayout.getChildren().addAll(gameLayer, mainMenuLayer, overlayMenuLayer);
    }

    public StackPane getRoot() { return rootLayout; }

    // =================================================================
    // LAYOUT GIOCO (Tavolo)
    // =================================================================
    private void createGameLayer() {
        gameLayer = new BorderPane();
        gameLayer.setStyle("-fx-background-color: #22313f;");
        gameLayer.setVisible(false);

        VBox rightPanel = createRightPanel();
        gameLayer.setRight(rightPanel);

        // --- CENTRO (TAVOLO) ---
        VBox boardContainer = new VBox(10);
        boardContainer.setPadding(new Insets(10));
        boardContainer.setAlignment(Pos.CENTER);
        VBox.setVgrow(boardContainer, Priority.ALWAYS);

        // 1. BARRA FASI
        createPhaseBar();

        // 2. AREE AVVERSARIO
        opponentLifeLabel = new Label("AVVERSARIO: 20 ❤");
        opponentLifeLabel.setTextFill(Color.web("#e74c3c"));
        opponentLifeLabel.setFont(Font.font("Impact", 18));

        opponentHand = new HBox(-40); opponentHand.setAlignment(Pos.TOP_CENTER); opponentHand.setPrefHeight(80);
        for(int i=0; i<5; i++) opponentHand.getChildren().add(createCardBack());

        opponentLandRow = new HBox(15);
        opponentCombatRow = new HBox(15);
        VBox oppLandBox = setupZoneWrapper(opponentLandRow, "🌲 TERRE NEMICHE", "#c0392b");
        VBox oppCombatBox = setupZoneWrapper(opponentCombatRow, "⚔ FRONTE NEMICO", "#e74c3c");

        // 3. AREE GIOCATORE
        combatRow = new HBox(15);
        landRow = new HBox(15);
        VBox myCombatBox = setupZoneWrapper(combatRow, "⚔ FRONTE", "#2ecc71");
        VBox myLandBox = setupZoneWrapper(landRow, "🌲 TERRE", "#27ae60");

        // 4. MANA POOL INTERATTIVO
        HBox manaContainer = new HBox(10);
        manaContainer.setAlignment(Pos.CENTER);
        manaContainer.setPadding(new Insets(5));
        manaContainer.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");

        String[] symbols = {"☀", "💧", "💀", "🔥", "🌳", "💎"};
        String[] colors = {"#f1c40f", "#3498db", "#9b59b6", "#e74c3c", "#2ecc71", "#95a5a6"};

        for(int i=0; i<6; i++) {
            manaLabels[i] = createManaBox(symbols[i], colors[i]);
            manaContainer.getChildren().add(manaLabels[i]);
        }

        handZone = new HBox(-50);
        handZone.setPadding(new Insets(10)); handZone.setPrefHeight(140); handZone.setAlignment(Pos.BOTTOM_CENTER);
        handZone.setStyle("-fx-background-color: #34495e; -fx-background-radius: 15 15 0 0; -fx-border-color: #1abc9c; -fx-border-width: 2 2 0 2;");

        boardContainer.getChildren().addAll(
                phaseBar,
                opponentLifeLabel, opponentHand, oppLandBox, oppCombatBox,
                new Separator(),
                myCombatBox, myLandBox,
                manaContainer,
                handZone
        );
        gameLayer.setCenter(boardContainer);
    }

    // =================================================================
    // MENU PRINCIPALE (RIPRISTINATO ALLA VERSIONE BELLA)
    // =================================================================
    private void createMainMenuLayer() {
        mainMenuLayer = new VBox(30);
        mainMenuLayer.setAlignment(Pos.CENTER);
        // Sfondo Gradiente figo (NON nero piatto)
        mainMenuLayer.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c3e50, #000000);");

        Label title = new Label("J-CARDBATTLE");
        title.setFont(Font.font("Impact", 80));
        title.setTextFill(Color.WHITE);
        // Effetto Ombra ripristinato
        title.setEffect(new DropShadow(20, Color.BLACK));

        // Bottoni Grandi e Colorati
        btnSinglePlayer = createStyledButton("⚔ SINGLE PLAYER", "#e67e22");
        btnMultiPlayer = createStyledButton("🌐 GIOCA ONLINE", "#2980b9");
        btnExit = createStyledButton("❌ ESCI", "#c0392b");

        mainMenuLayer.getChildren().addAll(title, new Separator(), btnSinglePlayer, btnMultiPlayer, btnExit);
    }

    // =================================================================
    // PANNELLO DESTRO (CONTROLLI)
    // =================================================================
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setPrefWidth(260);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #1abc9c; -fx-border-width: 0 0 0 2;");

        btnPause = new Button("⚙ MENU");
        styleButtonSmall(btnPause, "#95a5a6");

        lifeLabel = new Label("TU: 20 ❤");
        lifeLabel.setTextFill(Color.web("#2ecc71"));
        lifeLabel.setFont(Font.font("Impact", 35));

        btnMinusLife = new Button("-");
        btnPlusLife = new Button("+");
        styleCircleButton(btnMinusLife, "#e74c3c");
        styleCircleButton(btnPlusLife, "#27ae60");
        HBox lifeControls = new HBox(20, btnMinusLife, btnPlusLife);
        lifeControls.setAlignment(Pos.CENTER);
        VBox lifeBox = new VBox(5, lifeLabel, lifeControls);
        lifeBox.setAlignment(Pos.CENTER);
        lifeBox.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-padding: 10; -fx-background-radius: 10;");

        deckVisual = new StackPane();
        deckVisual.setPrefSize(100, 140); deckVisual.setMaxSize(100, 140);
        deckVisual.setStyle("-fx-background-color: linear-gradient(to bottom right, #2980b9, #2c3e50); -fx-border-color: white; -fx-border-width: 4; -fx-border-radius: 10; -fx-cursor: hand;");
        Label deckLogo = new Label("J"); deckLogo.setFont(Font.font("Times", FontWeight.BOLD, 50)); deckLogo.setTextFill(Color.rgb(255,255,255,0.2));
        deckCountLabel = new Label("40"); deckCountLabel.setStyle("-fx-text-fill: white; -fx-background-color: black; -fx-padding: 2 5;");
        StackPane.setAlignment(deckCountLabel, Pos.BOTTOM_RIGHT);
        deckVisual.getChildren().addAll(deckLogo, deckCountLabel);

        btnShuffle = new Button("🔀 MESCOLA");
        styleButtonSmall(btnShuffle, "#8e44ad");

        searchField = new TextField(); searchField.setPromptText("Cerca...");
        btnSearch = new Button("🔍");
        styleButtonSmall(btnSearch, "#f39c12");
        HBox searchBox = new HBox(5, searchField, btnSearch);
        searchBox.setAlignment(Pos.CENTER);

        gameLog = new TextArea();
        gameLog.setEditable(false);
        gameLog.setWrapText(true);
        VBox.setVgrow(gameLog, Priority.ALWAYS);
        gameLog.setStyle("-fx-control-inner-background: #34495e; -fx-text-fill: white;");

        rightPanel.getChildren().addAll(
                btnPause,
                new Separator(),
                lifeBox,
                new Separator(),
                new Label("MAZZO"), deckVisual, btnShuffle,
                new Separator(),
                searchBox,
                new Separator(),
                new Label("LOG"), gameLog
        );
        return rightPanel;
    }

    // =================================================================
    // UTILITIES GRAFICHE
    // =================================================================
    private void createPhaseBar() {
        phaseBar = new HBox(15);
        phaseBar.setAlignment(Pos.CENTER);
        phaseBar.setPadding(new Insets(5));
        phaseBar.setStyle("-fx-background-color: #000000; -fx-background-radius: 0 0 10 10;");

        String[] phases = {"UNTAP", "DRAW", "MAIN 1", "COMBAT", "MAIN 2", "END"};
        for (String p : phases) {
            Label lbl = new Label(p);
            lbl.setTextFill(Color.GRAY);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            phaseLabels.put(p, lbl);
            phaseBar.getChildren().add(lbl);
        }

        btnNextPhase = new Button("▶ PASSA FASE");
        btnNextPhase.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        phaseBar.getChildren().add(btnNextPhase);
    }

    public void highlightPhase(String phaseName) {
        phaseLabels.values().forEach(l -> {
            l.setTextFill(Color.GRAY);
            l.setStyle("");
        });
        if(phaseLabels.containsKey(phaseName)) {
            Label active = phaseLabels.get(phaseName);
            active.setTextFill(Color.WHITE);
            active.setStyle("-fx-underline: true; -fx-effect: dropshadow(three-pass-box, white, 10, 0, 0, 0);");
        }
    }

    private Label createManaBox(String symbol, String colorHex) {
        Label lbl = new Label(symbol + " 0");
        lbl.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        lbl.setTextFill(Color.web(colorHex));
        lbl.setPadding(new Insets(5, 10, 5, 10));
        lbl.setStyle("-fx-border-color: " + colorHex + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: rgba(0,0,0,0.3); -fx-cursor: hand;");
        Tooltip.install(lbl, new Tooltip("SX: +1 | DX: -1"));
        return lbl;
    }

    private VBox setupZoneWrapper(HBox row, String title, String colorHex) {
        row.setPrefHeight(90); row.setPrefWidth(800); row.setAlignment(Pos.CENTER);
        row.setStyle("-fx-border-color: " + colorHex + "; -fx-border-width: 2; -fx-background-color: " + colorHex + "22; -fx-background-radius: 5;");
        Label label = new Label(title); label.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-size: 10px;");
        VBox wrapper = new VBox(2, label, row); wrapper.setAlignment(Pos.CENTER); VBox.setVgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    private Node createCardBack() {
        StackPane card = new StackPane(); card.setPrefSize(60, 90);
        Rectangle bg = new Rectangle(60, 90); bg.setFill(Color.DARKBLUE); bg.setStroke(Color.WHITE); bg.setArcWidth(10); bg.setArcHeight(10);
        card.getChildren().add(bg); return card;
    }

    private void createOverlayLayer() {
        overlayMenuLayer = new VBox(20); overlayMenuLayer.setAlignment(Pos.CENTER);
        overlayMenuLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); overlayMenuLayer.setVisible(false);
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(300); btn.setPrefHeight(60);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
        return btn;
    }

    private void styleButtonSmall(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    private void styleCircleButton(Button btn, String color) {
        btn.setStyle("-fx-background-radius: 50; -fx-min-width: 40px; -fx-min-height: 40px; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-cursor: hand; -fx-background-color: " + color + ";");
    }

    // --- AGGIORNA GRAFICA ---
    public void updateManaDisplay(int[] pool) {
        String[] symbols = {"☀", "💧", "💀", "🔥", "🌳", "💎"};
        for(int i=0; i<6; i++) manaLabels[i].setText(symbols[i] + " " + pool[i]);
    }

    // --- GETTERS & SETTERS ---
    public void showGame() { mainMenuLayer.setVisible(false); overlayMenuLayer.setVisible(false); gameLayer.setVisible(true); }
    public void showMenu() { gameLayer.setVisible(false); overlayMenuLayer.setVisible(false); mainMenuLayer.setVisible(true); }
    public void showOverlay(Node content) { overlayMenuLayer.getChildren().clear(); overlayMenuLayer.getChildren().add(content); overlayMenuLayer.setVisible(true); }
    public void hideOverlay() { overlayMenuLayer.setVisible(false); }
    public void updateLife(int life) { lifeLabel.setText("TU: " + life + " ❤"); if (life <= 10) lifeLabel.setTextFill(Color.RED); else lifeLabel.setTextFill(Color.web("#2ecc71")); }
    public void updateDeckCount(int count) { deckCountLabel.setText(String.valueOf(count)); }
    public void log(String msg) { gameLog.appendText("> " + msg + "\n"); gameLog.setScrollTop(Double.MAX_VALUE); }
    public void animateDeckClick() { deckVisual.setTranslateY(4); new Timer().schedule(new TimerTask() { @Override public void run() { Platform.runLater(() -> deckVisual.setTranslateY(0)); }}, 100); }

    public Button getBtnSinglePlayer() { return btnSinglePlayer; }
    public Button getBtnMultiPlayer() { return btnMultiPlayer; }
    public Button getBtnExit() { return btnExit; }
    public Button getBtnSearch() { return btnSearch; }
    public Button getBtnPause() { return btnPause; }
    public Button getBtnMinusLife() { return btnMinusLife; }
    public Button getBtnPlusLife() { return btnPlusLife; }
    public Button getBtnShuffle() { return btnShuffle; }
    public Button getBtnNextPhase() { return btnNextPhase; }
    public Label[] getManaLabels() { return manaLabels; } // NECESSARIO PER IL MAIN

    public StackPane getDeckVisual() { return deckVisual; }
    public TextField getSearchField() { return searchField; }
    public HBox getHandZone() { return handZone; }
    public HBox getCombatRow() { return combatRow; }
    public HBox getLandRow() { return landRow; }
}