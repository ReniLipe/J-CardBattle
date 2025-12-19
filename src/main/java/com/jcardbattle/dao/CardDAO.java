package com.jcardbattle.dao;

import com.jcardbattle.model.Card;
import java.util.List;

public interface CardDAO {
    // Qui elenchiamo COSA possiamo fare, non COME
    List<Card> getAllCards();
}