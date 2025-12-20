package com.jcardbattle;

import com.jcardbattle.dao.CardDAO;
import com.jcardbattle.dao.CardDAOImpl;
import com.jcardbattle.model.Card;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// 1. Estendiamo "Application" per dire che è un programma grafico
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 2. Prepariamo i dati (Backend)
        CardDAO cardDAO = new CardDAOImpl();
        List<Card> cards = cardDAO.getAllCards();

        // 3. Creiamo la componente grafica (Lista visiva)
        ListView<String> listView = new ListView<>();

        // Riempiamo la lista con i nomi delle carte
        for (Card c : cards) {
            // Aggiungiamo una stringa descrittiva per ogni carta
            listView.getItems().add(c.toString() + " - " + c.getDescription());
        }

        // 4. Creiamo il Layout (una scatola verticale)
        VBox root = new VBox(10); // 10 è lo spazio tra gli elementi
        Label titleLabel = new Label("J-CardBattle - Collezione Carte");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;"); // Un po' di CSS inline

        root.getChildren().addAll(titleLabel, listView);

        // 5. Creiamo la Scena (la finestra vera e propria)
        Scene scene = new Scene(root, 600, 400); // Larghezza 600, Altezza 400

        primaryStage.setTitle("J-CardBattle v1.0");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Questo avvia JavaFX
    }
}