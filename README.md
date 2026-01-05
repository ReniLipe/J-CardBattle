(devi cambiare la password nel file databaseconnection nella cartella DAO e mettere quella del tuo servers mysql)
qui sono i tre file sql da mettere su mysql per fare il test...

-------SQL File 4*---------
INSERT INTO cards (name, card_type, mana_cost, attack, health, description) VALUES 
-- TERRE
('Foresta Antica', 'LAND', 0, 0, 0, 'Fornisce Mana Verde.'),
('Montagna Rossa', 'LAND', 0, 0, 0, 'Fornisce Mana Rosso.'),
('Isola Perduta', 'LAND', 0, 0, 0, 'Fornisce Mana Blu.'),
('Palude Nera', 'LAND', 0, 0, 0, 'Fornisce Mana Nero.'),
('Tempio Sacro', 'LAND', 0, 0, 0, 'Fornisce Mana Bianco.'),

-- MINIONS COSTO BASSO
('Goblin Esploratore', 'MINION', 1, 1, 1, 'Veloce e fastidioso.'),
('Lupo Grigio', 'MINION', 1, 2, 1, 'Attacca in branco.'),
('Scudiero Fedele', 'MINION', 1, 1, 3, 'Ottima difesa iniziale.'),
('Elfo Arciere', 'MINION', 2, 3, 1, 'Colpisce da lontano.'),
('Ragno Gigante', 'MINION', 2, 2, 3, 'Tesse ragnatele.'),
('Slime Acido', 'MINION', 2, 1, 4, 'Difficile da uccidere.'),
('Spadaccino Reale', 'MINION', 2, 3, 2, 'Addestrato al combattimento.'),

-- MINIONS COSTO MEDIO
('Orco Guerriero', 'MINION', 3, 4, 3, 'Brutale forza fisica.'),
('Mago del Fuoco', 'MINION', 3, 3, 2, 'I suoi attacchi bruciano.'),
('Cavaliere Bardato', 'MINION', 4, 3, 5, 'Armatura pesante.'),
('bambina', 'MINION', 4, 4, 4, 'Domina i cieli.'),
('necrofilo', 'MINION', 4, 2, 4, 'Evoca i morti.'),
('Cacciatore di Teste', 'MINION', 3, 5, 2, 'Alto attacco, bassa difesa.'),
('Golem di Argilla', 'MINION', 4, 2, 6, 'Un muro vivente.'),

-- MINIONS COSTO ALTO (BOSS)
('Drago Rosso', 'MINION', 6, 7, 6, 'Il re delle montagne.'),
('Leviatano', 'MINION', 7, 6, 9, 'Mostro degli abissi.'),
('Gigante di Pietra', 'MINION', 6, 5, 8, 'Lento ma inarrestabile.'),
('Angelo Custode', 'MINION', 5, 5, 5, 'Protegge gli alleati.'),
('Demone del Pozzo', 'MINION', 8, 8, 8, 'Distruzione pura.'),

-- SPELLS (MAGIE)
('Palla di Fuoco', 'SPELL', 2, 0, 0, 'Infligge 3 danni.'),
('Guarigione Rapida', 'SPELL', 1, 0, 0, 'Cura 4 punti vita.'),
('Congelamento', 'SPELL', 3, 0, 0, 'Blocca un nemico.'),
('Forza del Toro', 'SPELL', 2, 0, 0, '+3 Attacco a un minion.'),
('Scudo Divino', 'SPELL', 2, 0, 0, 'Rende invulnerabile per un turno.'),
('Fulmine', 'SPELL', 4, 0, 0, 'Infligge 5 danni.'),
('Risveglio dei Morti', 'SPELL', 5, 0, 0, 'Riporta in vita un minion.'),
('Pioggia di Frecce', 'SPELL', 3, 0, 0, '1 danno a tutti i nemici.'),
('Benedizione', 'SPELL', 1, 0, 0, '+1/+1 a tutti i tuoi minion.'),
('Maledizione', 'SPELL', 2, 0, 0, '-2 Attacco a un nemico.'),
('Esplosione Arcana', 'SPELL', 6, 0, 0, '10 danni diretti.'),
('Teletrasporto', 'SPELL', 2, 0, 0, 'Rimetti un minion in mano.'),
('Furia', 'SPELL', 3, 0, 0, 'Un minion attacca due volte.');

-- 4. SETUP ADMIN E MAZZO

INSERT INTO users (username, password) VALUES ('admin', 'admin');
INSERT INTO decks (user_id, name) VALUES (1, 'Mazzo Start');

-- 5. RIEMPIMENTO MAZZO (Importante!)

-- Mette TUTTE le carte create nel mazzo
INSERT INTO deck_composition (deck_id, card_id) SELECT 1, id FROM cards;

-- Aggiunge terre extra per bilanciare (altrimenti hai poco mana)
INSERT INTO deck_composition (deck_id, card_id) SELECT 1, id FROM cards WHERE card_type = 'LAND';
INSERT INTO deck_composition (deck_id, card_id) SELECT 1, id FROM cards WHERE card_type = 'LAND';

-----------------Query 1----------------
-- =======================================================
-- SQL FILE 4: SETUP COMPLETO (Struttura + 40 Carte)
-- =======================================================

-- 1. RESET E CREAZIONE DATABASE
DROP DATABASE IF EXISTS jcardbattle_db;
CREATE DATABASE jcardbattle_db;
USE jcardbattle_db;

-- 2. CREAZIONE TABELLE

-- Tabella CARTE
CREATE TABLE cards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    card_type ENUM('LAND', 'MINION', 'SPELL') NOT NULL, 
    mana_cost INT DEFAULT 0,
    attack INT DEFAULT 0,
    health INT DEFAULT 0,
    description VARCHAR(255)
);

-- Tabella UTENTI
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, 
    gold INT DEFAULT 100
);

-- Tabella MAZZI
CREATE TABLE decks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    name VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabella COMPOSIZIONE MAZZO
CREATE TABLE deck_composition (
    deck_id INT,
    card_id INT,
    FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Tabella COLLEZIONE UTENTE
CREATE TABLE user_collection (
    user_id INT,
    card_id INT,
    quantity INT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, card_id)
);



--------------------cards---------------------
SELECT * FROM jcardbattle_db.cards;cardsdeck_compositiondeck_compositiondecksdeckssys_config


