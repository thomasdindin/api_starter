package fr.thomasdindin.api_starter.audit.service;

import fr.thomasdindin.api_starter.audit.AuditAction;
import fr.thomasdindin.api_starter.audit.AuditLog;
import fr.thomasdindin.api_starter.audit.repository.AuditLogRepository;
import fr.thomasdindin.api_starter.dto.AuditLogDto;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.mappers.AuditLogMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(AuditAction action, Utilisateur utilisateur, String adresseIp) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUtilisateur(utilisateur);
        auditLog.setAdresseIp(adresseIp);
        auditLog.setDateAction(Instant.now());
        auditLogRepository.save(auditLog);
    }

    public List<AuditLogDto> findAll() {
        return auditLogRepository.findAll().stream().map(AuditLogMapper::toDto).toList();
    }
}