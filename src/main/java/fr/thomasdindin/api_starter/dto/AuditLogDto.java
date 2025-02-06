package fr.thomasdindin.api_starter.dto;

import fr.thomasdindin.api_starter.audit.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private UtilisateurDto utilisateur;
    private AuditAction action;
    private String adresseIp;
    private Instant dateAction;
}
