CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),                           -- Identifiant unique du token
    user_id UUID NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,      -- Lien avec l'utilisateur
    token_hash TEXT NOT NULL,                                                -- Hash du refreshToken pour éviter de stocker le token brut
    device_info TEXT NOT NULL,                                               -- Empreinte unique de l'appareil (User-Agent, hash IP…)
    ip_address TEXT,                                                         -- Adresse IP de la connexion
    last_used TIMESTAMP DEFAULT NOW(),                                       -- Dernière utilisation pour un refresh
    UNIQUE (user_id, token_hash)                                             -- Un même utilisateur ne peut pas avoir deux fois le même token
);
