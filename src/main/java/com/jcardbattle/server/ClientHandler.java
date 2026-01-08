package com.jcardbattle.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private Socket socket;
    private List<ClientHandler> allClients;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, List<ClientHandler> allClients) {
        this.socket = socket;
        this.allClients = allClients;

        // --- MODIFICA: Inizializziamo SUBITO i canali qui ---
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // (L'inizializzazione è stata spostata sopra, quindi qui l'ho tolta)

            sendMessage("BENVENUTO NEL SERVER! Sei connesso.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Messaggio ricevuto: " + message);
                GameServer.broadcast(message, this);
            }

        } catch (IOException e) {
            System.out.println("Giocatore disconnesso.");
        } finally {
            try {
                socket.close();
                allClients.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    // Metodo aggiunto per risolvere il tuo errore
    public String receiveMessage() {
        try {
            if (in != null) return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Metodo aggiunto prima
    public String getAddress() {
        if (socket != null) return socket.getInetAddress().toString();
        return "Unknown";
    }
}