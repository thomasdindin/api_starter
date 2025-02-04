CREATE TABLE password_reset (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                utilisateur_id UUID NOT NULL,
                                token VARCHAR(64) NOT NULL,
                                date_expiration TIMESTAMP DEFAULT (NOW() + INTERVAL '1 HOUR'),
                                used BOOLEAN DEFAULT FALSE,
                                CONSTRAINT fk_utilisateur_reset FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE
);

COMMENT ON COLUMN password_reset.token IS 'Token unique envoyé pour la réinitialisation du mot de passe.';
COMMENT ON COLUMN password_reset.date_expiration IS 'Date limite avant laquelle le token doit être utilisé.';
COMMENT ON COLUMN password_reset.used IS 'Indique si le token a été utilisé pour réinitialiser le mot de passe.';
