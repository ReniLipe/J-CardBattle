package com.jcardbattle.network;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived; // Funzione da chiamare quando arriva un messaggio

    public NetworkClient(String serverAddress, int serverPort, Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;

        try {
            // 1. Tenta la connessione al server
            this.socket = new Socket(serverAddress, serverPort);

            // 2. Prepara i canali di Input/Output
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 3. Avvia un Thread separato per ascoltare i messaggi in arrivo
            // (Così non blocchiamo il gioco mentre aspettiamo risposte)
            new Thread(this::listen).start();

        } catch (IOException e) {
            System.err.println("Impossibile connettersi al server: " + e.getMessage());
            // Notifica l'errore sulla grafica
            Platform.runLater(() -> onMessageReceived.accept("ERRORE: Impossibile connettersi al server."));
        }
    }

    // Loop infinito che ascolta il server
    private void listen() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                String finalMessage = message;
                // IMPORTANTE: Passa il messaggio al Thread della grafica
                Platform.runLater(() -> onMessageReceived.accept(finalMessage));
            }
        } catch (IOException e) {
            Platform.runLater(() -> onMessageReceived.accept("DISCONNESSO dal server."));
        }
    }

    // Metodo per inviare messaggi al server
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}