package com.jcardbattle.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    private static final int PORT = 12345; // La porta di ingresso del server
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(">>> SERVER J-CARDBATTLE AVVIATO SULLA PORTA " + PORT + " <<<");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Rimane in attesa che qualcuno si connetta...
                System.out.println("In attesa di giocatori...");
                Socket socket = serverSocket.accept();

                System.out.println("Nuovo giocatore connesso: " + socket.getInetAddress());

                // Crea un gestore per questo giocatore
                ClientHandler client = new ClientHandler(socket, clients);
                clients.add(client);

                // Avvia il thread per ascoltare questo giocatore
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per inviare un messaggio a TUTTI (Broadcast)
    // Es: "Giocatore 1 ha passato il turno" -> Lo devono sapere tutti
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            // Mandiamo il messaggio a tutti tranne a chi l'ha inviato (opzionale, ma utile per evitare eco)
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
}