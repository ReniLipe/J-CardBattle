package com.jcardbattle;

public class AppLauncher {
    public static void main(String[] args) {
        // Questo è il trucco: chiamiamo il Main vero da qui.
        // In questo modo Java ha il tempo di caricare le librerie Maven prima di avviare la grafica.
        Main.main(args);
    }
}