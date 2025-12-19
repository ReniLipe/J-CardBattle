package com.jcardbattle;

import com.jcardbattle.dao.CardDAO;
import com.jcardbattle.dao.CardDAOImpl;
import com.jcardbattle.model.Card;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- J-CardBattle Engine Started ---");

        // 1. Chiamo l'esperto (il DAO)
        CardDAO cardDAO = new CardDAOImpl();

        // 2. Gli chiedo la lista (Non mi interessa se usa MySQL, file o magia)
        List<Card> collezioneCompleta = cardDAO.getAllCards();

        // 3. Uso i dati
        System.out.println("Carte caricate: " + collezioneCompleta.size());

        for (Card c : collezioneCompleta) {
            System.out.println("- " + c);
        }
    }
}