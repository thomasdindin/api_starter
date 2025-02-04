package fr.thomasdindin.api_starter.repositories;

import fr.thomasdindin.api_starter.entities.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, UUID> {
    // Vous pourrez ajouter des méthodes de recherche (par exemple, findByToken) si besoin
}
