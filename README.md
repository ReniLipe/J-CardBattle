(devi cambiare la password nel file databaseconnection nella cartella DAO e mettere quella del tuo servers mysql)
-- 1. Pulizia: Se avevi fatto prove, resettiamo tutto per partire puliti
DROP DATABASE IF EXISTS jcardbattle_db;
CREATE DATABASE jcardbattle_db;
USE jcardbattle_db;

-- 2. Tabella CARTE (Il cuore del sistema ibrido)
CREATE TABLE cards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    -- TIPO: 'LAND' (dà mana), 'MINION' (combatte), 'SPELL' (azione immediata)
    card_type ENUM('LAND', 'MINION', 'SPELL') NOT NULL,
    mana_cost INT DEFAULT 0, -- Le terre costano 0
    attack INT DEFAULT 0,    -- Solo per i Minion
    health INT DEFAULT 0,    -- Solo per i Minion
    description VARCHAR(255) -- Testo dell'effetto
);cards

-- 3. Tabella UTENTI
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, 
    gold INT DEFAULT 100 -- Soldi iniziali per comprare pacchetti
);

-- 4. Tabella MAZZI
CREATE TABLE decks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    name VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 5. Tabella COMPOSIZIONE MAZZO (Quali carte sono nel mazzo)
CREATE TABLE deck_composition (
    deck_id INT,
    card_id INT,
    FOREIGN KEY (deck_id) REFERENCES decks(id),
    FOREIGN KEY (card_id) REFERENCES cards(id)
);

-- 6. Tabella COLLEZIONE UTENTE (Tutte le carte che possiedi)
CREATE TABLE user_collection (
    user_id INT,
    card_id INT,
    quantity INT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (card_id) REFERENCES cards(id)
);

-- =======================================================
-- STEP 2: INSERIMENTO DATI (Le tue prime carte)
-- =======================================================

INSERT INTO cards (name, card_type, mana_cost, attack, health, description) VALUES 
-- A. LE TERRE (Danno Mana come su Magic)
('Pianura', 'LAND', 0, 0, 0, 'Aumenta il tuo Mana massimo di 1.'),
('Isola Vulcanica', 'LAND', 0, 0, 0, 'Aumenta il tuo Mana massimo di 1.'),

-- B. I MINION (Combattenti come su Hearthstone)
('Soldato Semplice', 'MINION', 1, 1, 2, 'Un soldato base.'),
('Cavaliere del Drago', 'MINION', 4, 4, 5, 'Carica: Può attaccare subito.'),
('Golem di Pietra', 'MINION', 6, 6, 7, 'Provocazione: I nemici devono attaccare lui.'),

-- C. LE SPELL (Magie istantanee)
('Palla di Fuoco', 'SPELL', 2, 0, 0, 'Infligge 3 danni a un bersaglio.'),
('Potenziamento', 'SPELL', 1, 0, 0, 'Dai +2/+2 a un tuo Minion.');

-- Creiamo anche un utente di prova (TU)
INSERT INTO users (username, password) VALUES ('admin', 'admin');
