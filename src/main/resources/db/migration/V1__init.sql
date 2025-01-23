-- Création de la table utilisateur
CREATE TABLE utilisateur (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Identifiant unique pour chaque utilisateur
                             email VARCHAR(255) NOT NULL UNIQUE,           -- Email de l'utilisateur (unique)
                             mot_de_passe VARCHAR(255) NOT NULL,           -- Mot de passe haché de l'utilisateur
                             sel VARCHAR(255) NOT NULL,                    -- Valeur de salage pour le mot de passe
                             role VARCHAR(50) DEFAULT 'USER',              -- Rôle de l'utilisateur : 'USER' ou 'ADMIN'
                             compte_active BOOLEAN DEFAULT FALSE,          -- Indique si le compte est activé après vérification email
                             compte_bloque BOOLEAN DEFAULT FALSE,          -- Indique si le compte est bloqué après trop de tentatives de connexion
                             tentatives_connexion SMALLINT DEFAULT 0,      -- Nombre de tentatives de connexion échouées
                             date_creation TIMESTAMP DEFAULT NOW(),        -- Date de création du compte
                             CONSTRAINT email_format CHECK (email ~* '^[^@]+@[^@]+\.[^@]+$') -- Vérifie le format de l'email
    );

COMMENT ON COLUMN utilisateur.email IS 'Adresse email unique de utilisateur.';
COMMENT ON COLUMN utilisateur.mot_de_passe IS 'Mot de passe stocké en hash (avec salage).';
COMMENT ON COLUMN utilisateur.sel IS 'Sel utilisé pour le hashage du mot de passe.';
COMMENT ON COLUMN utilisateur.role IS 'Détermine les permissions de utilisateur.';
COMMENT ON COLUMN utilisateur.compte_active IS 'Vrai si utilisateur a vérifié son adresse email.';
COMMENT ON COLUMN utilisateur.compte_bloque IS 'Indique si laccès au compte est temporairement désactivé.';
COMMENT ON COLUMN utilisateur.tentatives_connexion IS 'Nombre de tentatives échouées avant de bloquer le compte.';
COMMENT ON COLUMN utilisateur.date_creation IS 'Date à laquelle le compte a été créé.';

-- Table pour stocker les tokens JWT
CREATE TABLE jwt_token (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- Identifiant unique
                           utilisateur_id UUID NOT NULL,                   -- Liaison avec l'utilisateur
                           token VARCHAR(500) NOT NULL,                    -- Le jeton JWT
                           date_expiration TIMESTAMP NOT NULL,             -- Date d'expiration du jeton
                           valide BOOLEAN DEFAULT TRUE,                    -- Indique si le token est toujours valide
                           CONSTRAINT fk_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE
);

COMMENT ON COLUMN jwt_token.token IS 'Jeton JWT utilisé pour authentification.';
COMMENT ON COLUMN jwt_token.date_expiration IS 'La date limite avant lexpiration du token.';
COMMENT ON COLUMN jwt_token.valide IS 'Indique si le token est invalide ou a été révoqué.';

-- Table pour gérer les logs d'audit
CREATE TABLE audit_log (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- Identifiant unique pour chaque log
                           utilisateur_id UUID,                            -- Liaison avec l'utilisateur si applicable
                           action VARCHAR(255) NOT NULL,                  -- Action réalisée par l'utilisateur ou le système
                           adresse_ip VARCHAR(50),                        -- Adresse IP de l'utilisateur ou du client
                           date_action TIMESTAMP DEFAULT NOW(),           -- Date et heure de l'action
                           CONSTRAINT fk_utilisateur_audit FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE SET NULL
);

COMMENT ON COLUMN audit_log.action IS 'Description de laction effectuée.';
COMMENT ON COLUMN audit_log.adresse_ip IS 'Adresse IP de la machine qui a réalisé action.';
COMMENT ON COLUMN audit_log.date_action IS 'Horodatage indiquant quand laction a eu lieu.';

-- Table pour stocker les emails de vérification
CREATE TABLE verification_email (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- Identifiant unique
    utilisateur_id UUID NOT NULL,                   -- Liaison avec l'utilisateur
    code VARCHAR(6) NOT NULL,                       -- Code unique pour vérifier l'email
    date_expiration TIMESTAMP DEFAULT (NOW() + INTERVAL '24 HOURS'), -- Date limite de validité
    verifie BOOLEAN DEFAULT FALSE,                  -- Statut de la vérification
    CONSTRAINT fk_utilisateur_verif FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE
);

COMMENT ON COLUMN verification_email.code IS 'Code unique envoyé à utilisateur pour vérifier son email.';
COMMENT ON COLUMN verification_email.date_expiration IS 'Date limite avant laquelle le code doit être utilisé.';
COMMENT ON COLUMN verification_email.verifie IS 'Indique si lemail a été vérifié avec succès.';
