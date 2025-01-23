package fr.thomasdindin.api_starter.repositories;

import fr.thomasdindin.api_starter.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {
}