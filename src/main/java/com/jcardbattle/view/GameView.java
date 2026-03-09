package com.jcardbattle.view;

import com.jcardbattle.model.Card;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class GameView {

    private StackPane rootLayout;
    private BorderPane gameLayer;
    private VBox mainMenuLayer;
    private VBox overlayMenuLayer;

    // ZONE TAVOLO
    private HBox handZone;
    private HBox opponentHand;

    // I DUE TAVOLI SANDBOX GEMELLI
    private Pane myPlaymat;
    private Pane oppPlaymat;

    // WRAPPER
    private StackPane handWrapper;

    // ZONE SPECIALI
    private GamePile playerGraveyard, playerExile, opponentGraveyard, opponentExile;

    // PANNELLO ANTEPRIMA
    private VBox cardPreviewPanel;

    // UI ELEMENTS
    private Label lifeLabel, opponentLifeLabel;
    private Label[] manaLabels = new Label[6];
    private Label[] opponentManaLabels = new Label[6];
    private HBox phaseBar;
    private Map<String, Label> phaseLabels = new HashMap<>();
    private Button btnNextPhase;

    private TextArea gameLog;
    private Label deckCountLabel;
    private StackPane deckVisual;
    private TextField searchField;
    private Button btnSinglePlayer, btnMultiPlayer, btnExit, btnSearch, btnPause, btnMinusLife, btnPlusLife, btnShuffle;

    public GameView() { initUI(); }

    private void initUI() {
        rootLayout = new StackPane();
        rootLayout.setStyle("-fx-background-color: #1a252f;");

        StackPane scalableContent = new StackPane();
        final double BASE_WIDTH = 1440;
        final double BASE_HEIGHT = 900; // Base 16:10
        scalableContent.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        scalableContent.setMinSize(BASE_WIDTH, BASE_HEIGHT);
        scalableContent.setMaxSize(BASE_WIDTH, BASE_HEIGHT);

        createGameLayer();
        createMainMenuLayer();
        createOverlayLayer();

        scalableContent.getChildren().addAll(gameLayer, mainMenuLayer, overlayMenuLayer);

        Group scaleGroup = new Group(scalableContent);
        rootLayout.getChildren().add(scaleGroup);

        // ASCOLTATORE INTELLIGENTE
        // Calcola lo scale separatamente per X e Y per coprire SEMPRE tutto lo schermo
        rootLayout.widthProperty().addListener((obs, oldVal, newVal) -> {
            double scaleX = newVal.doubleValue() / BASE_WIDTH;
            scaleGroup.setScaleX(scaleX);
        });

        rootLayout.heightProperty().addListener((obs, oldVal, newVal) -> {
            double scaleY = newVal.doubleValue() / BASE_HEIGHT;
            scaleGroup.setScaleY(scaleY);
        });
    }

    public StackPane getRoot() { return rootLayout; }

    private void createGameLayer() {
        gameLayer = new BorderPane();
        gameLayer.setStyle("-fx-background-color: #22313f;");
        gameLayer.setVisible(false);

        gameLayer.setRight(createRightPanel());
        gameLayer.setLeft(createLeftPanel());

        VBox boardContainer = new VBox(10);
        boardContainer.setPadding(new Insets(10));
        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.setMinWidth(0);
        VBox.setVgrow(boardContainer, Priority.ALWAYS);

        createPhaseBar();

        opponentLifeLabel = new Label("AVVERSARIO: 20 ❤"); opponentLifeLabel.setTextFill(Color.web("#e74c3c")); opponentLifeLabel.setFont(Font.font("Impact", 18));
        opponentHand = new HBox(-40); opponentHand.setAlignment(Pos.TOP_CENTER); opponentHand.setPrefHeight(80);
        HBox oppManaBox = createManaDisplay(opponentManaLabels, false);

        // CREAZIONE TAVOLO 16:9 GRIGIO
        oppPlaymat = new Pane();
        oppPlaymat.setPrefSize(800, 225);
        oppPlaymat.setMinSize(800, 225);
        oppPlaymat.setMaxSize(800, 225);

        myPlaymat = new Pane();
        myPlaymat.setPrefSize(800, 225);
        myPlaymat.setMinSize(800, 225);
        myPlaymat.setMaxSize(800, 225);

        VBox playmatContainer = new VBox(2);
        playmatContainer.setAlignment(Pos.CENTER);
        playmatContainer.setMaxSize(800, 452);
        playmatContainer.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #34495e; -fx-border-width: 4; -fx-background-radius: 10; -fx-border-radius: 10;");
        oppPlaymat.setStyle("-fx-background-color: #7f8c8d; -fx-background-radius: 5 5 0 0;");
        myPlaymat.setStyle("-fx-background-color: #7f8c8d; -fx-background-radius: 0 0 5 5;");
        playmatContainer.getChildren().addAll(oppPlaymat, myPlaymat);

        HBox myManaBox = createManaDisplay(manaLabels, true);

        // MANO DINAMICA
        handZone = new HBox(5);
        handZone.setPadding(new Insets(10));
        handZone.setAlignment(Pos.BOTTOM_CENTER);
        handZone.setMinWidth(0);

        handWrapper = new StackPane(handZone);
        handWrapper.setPrefHeight(160);
        handWrapper.setAlignment(Pos.BOTTOM_CENTER);
        handWrapper.setStyle("-fx-background-color: #34495e; -fx-background-radius: 15 15 0 0; -fx-border-color: #1abc9c; -fx-border-width: 2 2 0 2;");

        // La larghezza ora è FISSA (1440 di base - i pannelli laterali) = 840px circa
        handWrapper.setMaxWidth(840);
        handWrapper.setMinWidth(840);

        handZone.getChildren().addListener((ListChangeListener<Node>) c -> adjustHandLayout());

        boardContainer.getChildren().addAll(phaseBar, opponentLifeLabel, opponentHand, oppManaBox, playmatContainer, myManaBox, handWrapper);
        gameLayer.setCenter(boardContainer);
    }

    private void adjustHandLayout() {
        if (handZone == null) return;
        int numCards = handZone.getChildren().size();
        if (numCards == 0) return;

        // Lo spazio è sempre 840px virtuali
        double availableWidth = 840;
        double cardWidth = 100;
        double totalWidthNeeded = (numCards * cardWidth) + ((numCards - 1) * 5);

        if (totalWidthNeeded <= availableWidth) {
            handZone.setSpacing(5);
            handZone.setScaleX(1.0); handZone.setScaleY(1.0);
        } else {
            double newSpacing = (availableWidth - (numCards * cardWidth)) / Math.max(1, numCards - 1);
            if (newSpacing >= -60) {
                handZone.setSpacing(newSpacing);
                handZone.setScaleX(1.0); handZone.setScaleY(1.0);
            } else {
                handZone.setSpacing(-60);
                double compressedWidth = (numCards * cardWidth) + ((numCards - 1) * -60);
                double scaleFactor = Math.max(0.65, availableWidth / compressedWidth);
                handZone.setScaleX(scaleFactor); handZone.setScaleY(scaleFactor);
            }
        }
    }

    private VBox createLeftPanel() {
        VBox left = new VBox(15);
        left.setPadding(new Insets(15));
        left.setPrefWidth(250); left.setMinWidth(250); left.setMaxWidth(250);
        left.setAlignment(Pos.TOP_CENTER);
        left.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #1abc9c; -fx-border-width: 0 2 0 0;");

        cardPreviewPanel = new VBox();
        cardPreviewPanel.setPrefSize(230, 310);
        cardPreviewPanel.setAlignment(Pos.CENTER);
        cardPreviewPanel.setVisible(false); cardPreviewPanel.setManaged(false);

        opponentExile = new GamePile("ESILIO OPP.", "EXILE", "#8e44ad");
        opponentGraveyard = new GamePile("CIMITERO OPP.", "GRAVEYARD", "#7f8c8d");
        playerGraveyard = new GamePile("IL TUO CIMITERO", "GRAVEYARD", "#95a5a6");
        playerExile = new GamePile("IL TUO ESILIO", "EXILE", "#9b59b6");

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        left.getChildren().addAll(cardPreviewPanel, spacer, opponentExile, opponentGraveyard, new Region(), playerGraveyard, playerExile);
        return left;
    }

    private VBox createRightPanel() {
        VBox r = new VBox(15);
        r.setPadding(new Insets(15));
        r.setPrefWidth(260); r.setMinWidth(260); r.setMaxWidth(260);
        r.setAlignment(Pos.TOP_CENTER);
        r.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #1abc9c; -fx-border-width: 0 0 0 2;");

        btnPause = new Button("⚙ MENU"); styleButtonSmall(btnPause, "#95a5a6");
        lifeLabel = new Label("TU: 20 ❤"); lifeLabel.setTextFill(Color.web("#2ecc71")); lifeLabel.setFont(Font.font("Impact", 35));
        btnMinusLife = new Button("-"); btnPlusLife = new Button("+");
        styleCircleButton(btnMinusLife, "#e74c3c"); styleCircleButton(btnPlusLife, "#27ae60");
        HBox lc = new HBox(20, btnMinusLife, btnPlusLife); lc.setAlignment(Pos.CENTER);

        deckVisual = new StackPane(); deckVisual.setPrefSize(100, 140); deckVisual.setMaxSize(100, 140);
        deckVisual.setStyle("-fx-background-color: linear-gradient(to bottom right, #2980b9, #2c3e50); -fx-border-color: white; -fx-border-width: 4; -fx-border-radius: 10; -fx-cursor: hand;");
        deckCountLabel = new Label("40"); deckCountLabel.setStyle("-fx-text-fill: white; -fx-background-color: black; -fx-padding: 2 5;");
        StackPane.setAlignment(deckCountLabel, Pos.BOTTOM_RIGHT); deckVisual.getChildren().addAll(new Label("J"), deckCountLabel);

        btnShuffle = new Button("🔀 MESCOLA"); styleButtonSmall(btnShuffle, "#8e44ad");
        searchField = new TextField(); btnSearch = new Button("🔍"); styleButtonSmall(btnSearch, "#f39c12");
        HBox sb = new HBox(5, searchField, btnSearch); sb.setAlignment(Pos.CENTER);

        gameLog = new TextArea(); gameLog.setEditable(false); gameLog.setWrapText(true);
        VBox.setVgrow(gameLog, Priority.ALWAYS); gameLog.setStyle("-fx-control-inner-background: #34495e; -fx-text-fill: white;");

        r.getChildren().addAll(btnPause, new Separator(), lifeLabel, lc, new Separator(), new Label("MAZZO"), deckVisual, btnShuffle, new Separator(), sb, new Separator(), new Label("LOG"), gameLog);
        return r;
    }

    public void setCardPreview(Card card) {
        cardPreviewPanel.getChildren().clear();
        if (card != null) {
            VBox bigCard = new CardUI(card).createCardNode(true);
            cardPreviewPanel.getChildren().add(bigCard);
            cardPreviewPanel.setVisible(true); cardPreviewPanel.setManaged(true);
        } else {
            cardPreviewPanel.setVisible(false); cardPreviewPanel.setManaged(false);
        }
    }

    private void impostaTestoNero(Node node) {
        if (node instanceof Labeled) {
            Labeled l = (Labeled) node;
            l.setTextFill(Color.BLACK);
            String style = l.getStyle() == null ? "" : l.getStyle();
            l.setStyle(style + "; -fx-text-fill: black !important;");
        } else if (node instanceof javafx.scene.text.Text) {
            javafx.scene.text.Text t = (javafx.scene.text.Text) node;
            t.setFill(Color.BLACK);
            String style = t.getStyle() == null ? "" : t.getStyle();
            t.setStyle(style + "; -fx-fill: black !important;");
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                impostaTestoNero(child);
            }
        }
    }

    public void mostraBrowserCarte(String titolo, List<Card> carte, Consumer<Card> onCardClick) {
        Platform.runLater(() -> {
            VBox container = new VBox(15); container.setAlignment(Pos.CENTER); container.setStyle("-fx-background-color: rgba(0,0,0,0.95); -fx-padding: 30; -fx-background-radius: 15; -fx-border-color: white; -fx-border-width: 2;"); container.setMaxSize(900, 600);
            Label lblTitolo = new Label(titolo); lblTitolo.setFont(Font.font("Impact", 30)); lblTitolo.setTextFill(Color.WHITE);
            FlowPane flow = new FlowPane(); flow.setHgap(15); flow.setVgap(15); flow.setAlignment(Pos.CENTER); flow.setPrefWrapLength(800); flow.setPadding(new Insets(20));

            if (carte.isEmpty()) {
                Label empty = new Label("Nessuna carta presente."); empty.setTextFill(Color.LIGHTGRAY); empty.setFont(Font.font(18)); flow.getChildren().add(empty);
            } else {
                for (Card c : carte) {
                    VBox cardNode = new CardUI(c).createCardNode(false);
                    impostaTestoNero(cardNode);
                    cardNode.setCursor(Cursor.HAND);
                    cardNode.setOnMouseClicked(e -> { hideOverlay(); onCardClick.accept(c); });
                    Tooltip.install(cardNode, new Tooltip("Clicca per riprendere in mano"));
                    flow.getChildren().add(cardNode);
                }
            }
            ScrollPane scroll = new ScrollPane(flow); scroll.setFitToWidth(true); scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;"); scroll.setPannable(true); VBox.setVgrow(scroll, Priority.ALWAYS);
            Button btnClose = new Button("CHIUDI"); styleButtonSmall(btnClose, "#c0392b"); btnClose.setOnAction(e -> hideOverlay());
            container.getChildren().addAll(lblTitolo, scroll, btnClose); showOverlay(container);
        });
    }

    public void addOpponentCard(String cardName, double oppX, double oppY) {
        Platform.runLater(() -> {
            StackPane c = new StackPane();
            c.setPrefSize(50, 70);
            Rectangle r = new Rectangle(50, 70); r.setFill(Color.WHITE); r.setStroke(Color.BLACK);
            Label l = new Label(cardName); l.setWrapText(true); l.setMaxWidth(45); l.setAlignment(Pos.CENTER); l.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); l.setFont(Font.font(9));
            c.getChildren().addAll(r, l);
            c.setLayoutX(800 - oppX - 50); c.setLayoutY(225 - oppY - 70);
            oppPlaymat.getChildren().add(c);
        });
    }

    public void removeOpponentCard(String cardName) {
        Platform.runLater(() -> {
            Node target = null;
            for(Node n : oppPlaymat.getChildren()) {
                if(n instanceof StackPane) {
                    for(Node child : ((StackPane)n).getChildren()) {
                        if(child instanceof Label && ((Label)child).getText().trim().equalsIgnoreCase(cardName.trim())) { target = n; break; }
                    }
                }
                if(target!=null) break;
            }
            if(target!=null) oppPlaymat.getChildren().remove(target);
        });
    }

    public void rotateOpponentCard(String cardName, double angle) { Platform.runLater(() -> { for(Node n : oppPlaymat.getChildren()) { if(n instanceof StackPane) { for(Node child : ((StackPane)n).getChildren()) { if(child instanceof Label && ((Label)child).getText().trim().equalsIgnoreCase(cardName.trim())) { n.setRotate(angle); return; } } } } }); }
    public void untapAllOpponentCards() { Platform.runLater(() -> { for(Node n : oppPlaymat.getChildren()) { if (n instanceof StackPane) n.setRotate(0); } }); }
    public void addCardToOpponentPile(String pileType, String cardName) { GamePile target = pileType.equals("GRAVEYARD") ? opponentGraveyard : opponentExile; target.addVisualCard(cardName); }
    public void removeCardFromOpponentPile(String pileType) { GamePile target = pileType.equals("GRAVEYARD") ? opponentGraveyard : opponentExile; target.removeTopCard(); }
    public void updateOpponentLife(int life) { Platform.runLater(() -> opponentLifeLabel.setText("AVVERSARIO: " + life + " ❤")); }
    public void addOpponentHandCard() { Platform.runLater(() -> opponentHand.getChildren().add(createCardBack())); }
    public void removeOpponentHandCard() { Platform.runLater(() -> { if (!opponentHand.getChildren().isEmpty()) opponentHand.getChildren().remove(opponentHand.getChildren().size() - 1); }); }
    public void clearOpponentHand() { Platform.runLater(() -> opponentHand.getChildren().clear()); }
    public void setOpponentHandSize(int count) { Platform.runLater(() -> { opponentHand.getChildren().clear(); for(int i=0; i<count; i++) opponentHand.getChildren().add(createCardBack()); }); }
    public void showGame() { mainMenuLayer.setVisible(false); overlayMenuLayer.setVisible(false); gameLayer.setVisible(true); }
    public void showMenu() { gameLayer.setVisible(false); overlayMenuLayer.setVisible(false); mainMenuLayer.setVisible(true); }
    public void updateLife(int life) { Platform.runLater(() -> lifeLabel.setText("TU: " + life + " ❤")); }
    public void updateDeckCount(int count) { Platform.runLater(() -> deckCountLabel.setText(String.valueOf(count))); }

    private HBox createManaDisplay(Label[] labels, boolean interactive) {
        HBox box = new HBox(10); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(5)); box.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");
        String[] sym = {"☀", "💧", "💀", "🔥", "🌳", "💎"}; String[] cols = {"#f1c40f", "#3498db", "#9b59b6", "#e74c3c", "#2ecc71", "#95a5a6"};
        for(int i=0; i<6; i++) {
            labels[i] = new Label(sym[i]+" 0"); labels[i].setFont(Font.font("Consolas", FontWeight.BOLD, 16)); labels[i].setTextFill(Color.web(cols[i]));
            labels[i].setPadding(new Insets(3,8,3,8)); labels[i].setStyle("-fx-border-color:"+cols[i]+"; -fx-border-width:2; -fx-border-radius:5; -fx-background-color:rgba(0,0,0,0.3); "+(interactive?"-fx-cursor:hand;":""));
            if(interactive) Tooltip.install(labels[i], new Tooltip("SX:+1 DX:-1")); box.getChildren().add(labels[i]);
        } return box;
    }
    public void updateManaDisplay(int[] p) { for(int i=0; i<6; i++) manaLabels[i].setText((new String[]{"☀", "💧", "💀", "🔥", "🌳", "💎"})[i]+" "+p[i]); }
    public void updateOpponentManaDisplay(int[] p) { Platform.runLater(()->{ for(int i=0; i<6; i++) opponentManaLabels[i].setText((new String[]{"☀", "💧", "💀", "🔥", "🌳", "💎"})[i]+" "+p[i]); }); }
    private void createMainMenuLayer() { mainMenuLayer = new VBox(30); mainMenuLayer.setAlignment(Pos.CENTER); mainMenuLayer.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c3e50, #000000);"); Label t = new Label("J-CARDBATTLE"); t.setFont(Font.font("Impact", 80)); t.setTextFill(Color.WHITE); t.setEffect(new DropShadow(20, Color.BLACK)); btnSinglePlayer = createStyledButton("⚔ SINGLE PLAYER", "#e67e22"); btnMultiPlayer = createStyledButton("🌐 GIOCA ONLINE", "#2980b9"); btnExit = createStyledButton("❌ ESCI", "#c0392b"); mainMenuLayer.getChildren().addAll(t, new Separator(), btnSinglePlayer, btnMultiPlayer, btnExit); }
    private void createPhaseBar() { phaseBar = new HBox(15); phaseBar.setAlignment(Pos.CENTER); phaseBar.setPadding(new Insets(5)); phaseBar.setStyle("-fx-background-color: #000000; -fx-background-radius: 0 0 10 10;"); String[] p = {"UNTAP", "DRAW", "MAIN 1", "COMBAT", "MAIN 2", "END"}; for(String s : p) { Label l = new Label(s); l.setTextFill(Color.GRAY); l.setFont(Font.font("Arial", FontWeight.BOLD, 12)); phaseLabels.put(s, l); phaseBar.getChildren().add(l); } btnNextPhase = new Button("▶ PASSA FASE"); btnNextPhase.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;"); phaseBar.getChildren().add(btnNextPhase); }
    public void highlightPhase(String name) { phaseLabels.values().forEach(l -> { l.setTextFill(Color.GRAY); l.setStyle(""); }); if(phaseLabels.containsKey(name)) { Label l = phaseLabels.get(name); l.setTextFill(Color.WHITE); l.setStyle("-fx-underline: true;"); } }
    private Node createCardBack() { StackPane c=new StackPane(); c.setPrefSize(60,90); Rectangle r=new Rectangle(60,90); r.setFill(Color.DARKBLUE); r.setStroke(Color.WHITE); c.getChildren().add(r); return c; }
    private void createOverlayLayer() { overlayMenuLayer=new VBox(20); overlayMenuLayer.setAlignment(Pos.CENTER); overlayMenuLayer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); overlayMenuLayer.setVisible(false); }
    public void showOverlay(Node n) { overlayMenuLayer.getChildren().clear(); overlayMenuLayer.getChildren().add(n); overlayMenuLayer.setVisible(true); }
    public void hideOverlay() { overlayMenuLayer.setVisible(false); }
    public void mostraSchermataFinePartita(String t, Color c) { Platform.runLater(() -> { VBox b = new VBox(20); b.setAlignment(Pos.CENTER); b.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-padding: 50; -fx-background-radius: 20;"); Label l = new Label(t); l.setFont(Font.font("Impact", 80)); l.setTextFill(c); Button m = new Button("MENU"); styleButtonSmall(m, "#e67e22"); m.setOnAction(e -> showMenu()); b.getChildren().addAll(l, m); showOverlay(b); }); }
    private Button createStyledButton(String t, String c) { Button b=new Button(t); b.setPrefWidth(300); b.setPrefHeight(60); b.setFont(Font.font("Arial", FontWeight.BOLD, 20)); b.setStyle("-fx-background-color:"+c+"; -fx-text-fill:white; -fx-background-radius:10; -fx-cursor:hand;"); return b; }
    private void styleButtonSmall(Button b, String c) { b.setStyle("-fx-background-color:"+c+"; -fx-text-fill: white; -fx-font-weight:bold; -fx-cursor:hand;"); }
    private void styleCircleButton(Button b, String c) { b.setStyle("-fx-background-radius:50; -fx-background-color:"+c+"; -fx-text-fill:white; -fx-min-width:40px; -fx-cursor:hand;"); }
    public void log(String m) { gameLog.appendText("> "+m+"\n"); }
    public void animateDeckClick() { deckVisual.setTranslateY(4); new Timer().schedule(new TimerTask() { public void run() { Platform.runLater(()->deckVisual.setTranslateY(0)); }}, 100); }

    public Button getBtnSinglePlayer() { return btnSinglePlayer; }
    public Button getBtnMultiPlayer() { return btnMultiPlayer; }
    public Button getBtnExit() { return btnExit; }
    public Button getBtnSearch() { return btnSearch; }
    public Button getBtnPause() { return btnPause; }
    public Button getBtnMinusLife() { return btnMinusLife; }
    public Button getBtnPlusLife() { return btnPlusLife; }
    public Button getBtnShuffle() { return btnShuffle; }
    public Button getBtnNextPhase() { return btnNextPhase; }
    public Label[] getManaLabels() { return manaLabels; }
    public StackPane getDeckVisual() { return deckVisual; }
    public TextField getSearchField() { return searchField; }
    public HBox getHandZone() { return handZone; }
    public Pane getMyPlaymat() { return myPlaymat; }
    public Pane getOppPlaymat() { return oppPlaymat; }
    public GamePile getPlayerGraveyard() { return playerGraveyard; }
    public GamePile getPlayerExile() { return playerExile; }
}