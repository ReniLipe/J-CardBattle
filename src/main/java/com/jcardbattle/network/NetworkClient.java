package com.jcardbattle.network;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived;

    // Variabile per controllare se il client deve ascoltare
    private boolean running = false;

    public NetworkClient(String serverAddress, int serverPort, Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;

        try {
            // 1. Tenta la connessione
            this.socket = new Socket(serverAddress, serverPort);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.running = true; // Attiva il flag

            // 2. Avvia il thread di ascolto
            new Thread(this::listen).start();

        } catch (IOException e) {
            System.err.println("Impossibile connettersi: " + e.getMessage());
            Platform.runLater(() -> onMessageReceived.accept("ERRORE: Impossibile connettersi al server."));
        }
    }

    private void listen() {
        try {
            String message;
            // Controlla anche 'running' per poter fermare il loop volontariamente
            while (running && (message = in.readLine()) != null) {
                String finalMessage = message;
                Platform.runLater(() -> {
                    if (onMessageReceived != null) onMessageReceived.accept(finalMessage);
                });
            }
        } catch (IOException e) {
            if (running) { // Se l'errore capita mentre dovevamo essere connessi
                Platform.runLater(() -> onMessageReceived.accept("DISCONNESSO dal server."));
            }
        } finally {
            close(); // Assicura la chiusura
        }
    }

    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    // --- NUOVO METODO FONDAMENTALE ---
    public void close() {
        running = false;
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}