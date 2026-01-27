package com.jcardbattle.server; // <--- CORRETTO: Deve corrispondere alla cartella!

import com.jcardbattle.server.GameServer; // Importiamo la classe GameServer
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GameServerTest {

    private Thread serverThread;
    private static final int TEST_PORT = 12345; // Porta usata dal server

    @BeforeEach
    void startServer() {
        // Avviamo il GameServer vero in un thread separato
        serverThread = new Thread(() -> {
            try {
                // NOTA: Qui assumiamo che GameServer abbia un main o un metodo start.
                // Se GameServer è nel package com.jcardbattle.server, Java ora lo troverà.
                GameServer.main(new String[]{});
            } catch (Exception e) {
                // Ignora errori di chiusura socket forzata
            }
        });
        serverThread.start();

        // Diamo 1 secondo al server per avviarsi prima di connetterci
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    @AfterEach
    void stopServer() {
        // Uccidiamo il thread alla fine del test
        if (serverThread != null) serverThread.interrupt();
    }

    @Test
    void testTwoPlayersCommunication() throws IOException, InterruptedException {
        // --- SIMULIAMO DUE GIOCATORI ---

        // 1. Connessione Player 1
        Socket p1 = new Socket("localhost", TEST_PORT);
        PrintWriter out1 = new PrintWriter(p1.getOutputStream(), true);

        // 2. Connessione Player 2
        Socket p2 = new Socket("localhost", TEST_PORT);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));

        // --- TEST BROADCAST ---
        // Player 1 manda un messaggio di gioco
        String msgToSend = "GIOCA:Goblin:COMBAT";
        out1.println(msgToSend);

        // Player 2 dovrebbe riceverlo?
        // Usiamo un Latch per aspettare il messaggio in modo asincrono
        CountDownLatch latch = new CountDownLatch(1);
        final String[] received = {null};

        new Thread(() -> {
            try {
                String line;
                while ((line = in2.readLine()) != null) {
                    // Ascoltiamo finché non arriva il messaggio giusto
                    if (line.contains("GIOCA:Goblin")) {
                        received[0] = line;
                        latch.countDown(); // Sblocca il test
                        break;
                    }
                }
            } catch (IOException e) {}
        }).start();

        // Aspettiamo max 2 secondi che il messaggio arrivi a P2
        boolean messageArrived = latch.await(2, TimeUnit.SECONDS);

        // Chiudiamo le connessioni
        p1.close();
        p2.close();

        // VERIFICHE
        assertTrue(messageArrived, "Il Player 2 dovrebbe aver ricevuto la mossa del Player 1");
        assertNotNull(received[0], "Il messaggio ricevuto è nullo");
        System.out.println("Test Superato: P2 ha ricevuto -> " + received[0]);
    }
}