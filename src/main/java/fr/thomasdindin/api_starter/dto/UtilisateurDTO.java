package fr.thomasdindin.api_starter.dto;

import java.time.Instant;
import java.util.UUID;

public record UtilisateurDTO(UUID uuid, String email, Instant dateCreation) {
}
