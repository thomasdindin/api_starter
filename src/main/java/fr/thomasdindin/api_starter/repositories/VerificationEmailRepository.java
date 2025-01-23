package fr.thomasdindin.api_starter.repositories;

import fr.thomasdindin.api_starter.entities.VerificationEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationEmailRepository extends JpaRepository<VerificationEmail, UUID> {
}