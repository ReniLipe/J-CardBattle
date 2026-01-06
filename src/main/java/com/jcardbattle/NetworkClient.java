package com.jcardbattle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverIp;
    private int serverPort;

    public NetworkClient(String ip, int port) {
        this.serverIp = ip;
        this.serverPort = port;
    }

    // Tenta di connettersi al server
    public boolean connect() {
        try {
            socket = new Socket(serverIp, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connesso al server!");

            // Avvia un thread che ascolta i messaggi in arrivo dal server
            new Thread(this::listenForMessages).start();
            return true;
        } catch (IOException e) {
            System.out.println("Impossibile connettersi al server: " + e.getMessage());
            return false;
        }
    }

    // Invia un messaggio al server (es. "GIOCA:Drago")
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    // Ascolta costantemente cosa dice il server
    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("SERVER DICE: " + message);

                // QUI GESTIREMO LA LOGICA!
                // Es. se message.startsWith("OPPONENT_PLAYED:") -> aggiorna grafica
            }
        } catch (IOException e) {
            System.out.println("Disconnesso dal server.");
        }
    }
}