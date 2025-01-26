CREATE TABLE blocked_ip (
                            id SERIAL PRIMARY KEY, -- Identifiant unique
                            adresse_ip VARCHAR(50) NOT NULL UNIQUE, -- Adresse IP bloquée (unique)
                            date_blocage TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Date de blocage
                            raison TEXT -- Raison pour laquelle l'adresse IP a été bloquée
);
