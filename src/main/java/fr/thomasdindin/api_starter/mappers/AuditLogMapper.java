package fr.thomasdindin.api_starter.mappers;

import fr.thomasdindin.api_starter.audit.AuditLog;
import fr.thomasdindin.api_starter.dto.AuditLogDto;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;

import java.util.Optional;

public class AuditLogMapper {
    public static AuditLogDto toDto(AuditLog auditLog) {
        Optional<Utilisateur> utilisateur = Optional.ofNullable(auditLog.getUtilisateur());
        return AuditLogDto.builder()
                .utilisateur(utilisateur.map(UtilisateurMapper::toDto).orElse(null))
                .action(auditLog.getAction())
                .adresseIp(auditLog.getAdresseIp())
                .dateAction(auditLog.getDateAction())
                .build();
    }
}
