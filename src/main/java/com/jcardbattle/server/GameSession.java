package com.jcardbattle.server;

public class GameSession extends Thread {
    private ClientHandler p1;
    private ClientHandler p2;

    public GameSession(ClientHandler p1, ClientHandler p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public void run() {
        try {
            System.out.println("Partita iniziata tra " + p1.getAddress() + " e " + p2.getAddress());

            // 1. Assegnazione Ruoli
            p1.sendMessage("START:PLAYER1"); // Tu inizi
            p2.sendMessage("START:PLAYER2"); // Tu aspetti

            // Qui in futuro metteremo il loop del gioco (Turno P1 -> Turno P2)
            // Per ora teniamo la connessione viva
            while (true) {
                String msgP1 = p1.receiveMessage();
                if (msgP1 != null) {
                    System.out.println("P1 dice: " + msgP1);
                    p2.sendMessage("OPPONENT_ACTION:" + msgP1); // Inoltra a P2
                }

                // Nota: In un server vero useremmo thread separati per leggere P1 e P2 contemporaneamente
            }

        } catch (Exception e) {
            System.out.println("Uno dei giocatori si è disconnesso.");
        }
    }
}