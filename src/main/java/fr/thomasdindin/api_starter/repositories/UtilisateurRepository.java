package fr.thomasdindin.api_starter.repositories;

import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findById(UUID id);

    @Query("SELECT MIN(u.dateCreation) FROM Utilisateur u")
    Optional<Instant> findMinDateCreation();
}