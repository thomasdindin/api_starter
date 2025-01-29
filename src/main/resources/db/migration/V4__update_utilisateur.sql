-- Création de la table Adresse
CREATE TABLE IF NOT EXISTS adresse (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero VARCHAR(10) NOT NULL,
                                       rue VARCHAR(255) NOT NULL,
                                       ville VARCHAR(100) NOT NULL,
                                       code_postal VARCHAR(20) NOT NULL,
                                       pays VARCHAR(100) NOT NULL,
                                       complement VARCHAR(255)
);

-- Ajout des nouvelles colonnes à la table Utilisateur
ALTER TABLE utilisateur
    ADD COLUMN prenom VARCHAR(50) NOT NULL DEFAULT 'Inconnu',
    ADD COLUMN nom VARCHAR(50) NOT NULL DEFAULT 'Inconnu',
    ADD COLUMN telephone VARCHAR(15),
    ADD COLUMN date_naissance TIMESTAMP NULL CHECK (date_naissance < NOW()),
    ADD COLUMN genre VARCHAR(10),
    ADD COLUMN photo_profil VARCHAR(255),
    ADD COLUMN adresse_id UUID NULL,
    ADD CONSTRAINT fk_utilisateur_adresse FOREIGN KEY (adresse_id) REFERENCES adresse (id) ON DELETE SET NULL;
