package com.jcardbattle.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private static final int PORT = 9999; // La porta di ascolto
    private static List<ClientHandler> codaAttesa = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("--- J-CARDBATTLE SERVER ONLINE ---");
        System.out.println("In attesa di giocatori sulla porta " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // 1. Aspetta che qualcuno si connetta
                Socket socket = serverSocket.accept();
                System.out.println("Nuova connessione: " + socket.getInetAddress());

                // 2. Crea il gestore
                ClientHandler player = new ClientHandler(socket);

                // 3. Matchmaking (Ne servono 2 per giocare)
                matchmaking(player);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void matchmaking(ClientHandler player) {
        if (codaAttesa.isEmpty()) {
            codaAttesa.add(player);
            player.sendMessage("WAIT:In attesa di un avversario...");
            System.out.println("Giocatore messo in coda.");
        } else {
            ClientHandler opponent = codaAttesa.remove(0);
            System.out.println("Avversario trovato! Creazione partita...");

            // Avvia la partita in un Thread separato
            new GameSession(opponent, player).start();
        }
    }
}