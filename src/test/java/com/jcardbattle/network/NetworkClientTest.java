package com.jcardbattle.network;

import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NetworkClientTest {

    private NetworkClient client;
    private ServerSocket mockServer;

    // --- FIX: INIZIALIZZA JAVAFX PER I TEST ---
    @BeforeAll
    static void initJfxRuntime() {
        try {
            // Avvia la piattaforma JavaFX
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Se è già avviata (es. da altri test), ignoriamo l'errore
        }
    }
    // ------------------------------------------

    @BeforeEach
    void setUp() throws IOException {
        // 1. Creiamo un Server Finto su una porta automatica
        mockServer = new ServerSocket(0);
        int port = mockServer.getLocalPort();

        // Avviamo il thread del server per accettare la connessione
        // Messaggio di prova
        // Server chiuso, normale
        Thread serverThread = new Thread(() -> {
            try {
                Socket connection = mockServer.accept();
                PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
                out.println("BENVENUTO_TEST"); // Messaggio di prova
            } catch (IOException e) {
                // Server chiuso, normale
            }
        });
        serverThread.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (client != null) client.close();
        if (mockServer != null) mockServer.close();
    }

    @Test
    void testConnectionAndReception() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] receivedMsg = {null};

        client = new NetworkClient("localhost", mockServer.getLocalPort(), message -> {
            // Questo gira nel thread JavaFX (Platform.runLater)
            System.out.println("Client ha ricevuto: " + message);
            receivedMsg[0] = message;
            latch.countDown();
        });

        // Aspetto che il messaggio arrivi (con un po' più di tolleranza)
        boolean messageArrived = latch.await(3, TimeUnit.SECONDS);

        assertTrue(messageArrived, "Il client non ha ricevuto il messaggio in tempo");
        assertEquals("BENVENUTO_TEST", receivedMsg[0]);
    }

    @Test
    void testSendMessage() throws IOException, InterruptedException {
        CountDownLatch serverLatch = new CountDownLatch(1);
        final String[] serverReceived = {null};

        // Chiudo il vecchio server per riaprirne uno che legge
        mockServer.close();
        mockServer = new ServerSocket(0);

        new Thread(() -> {
            try (Socket conn = mockServer.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                String line = in.readLine();
                serverReceived[0] = line;
                serverLatch.countDown();
            } catch(Exception e) {}
        }).start();

        client = new NetworkClient("localhost", mockServer.getLocalPort(), msg -> {});

        // Aspetto un attimo che il client si connetta davvero
        Thread.sleep(100);

        client.sendMessage("CIAO SERVER");

        boolean received = serverLatch.await(3, TimeUnit.SECONDS);

        assertTrue(received);
        assertEquals("CIAO SERVER", serverReceived[0]);
    }
}