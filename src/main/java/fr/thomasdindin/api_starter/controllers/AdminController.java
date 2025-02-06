package fr.thomasdindin.api_starter.controllers;

import fr.thomasdindin.api_starter.audit.service.AuditLogService;
import fr.thomasdindin.api_starter.audit.service.MonitoringService;
import fr.thomasdindin.api_starter.dto.AuditLogDto;
import fr.thomasdindin.api_starter.dto.BlacklistDto;
import fr.thomasdindin.api_starter.dto.MonitoringDto;
import fr.thomasdindin.api_starter.dto.UtilisateurDto;
import fr.thomasdindin.api_starter.services.BlacklistService;
import fr.thomasdindin.api_starter.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminController {

    private final AuditLogService auditLogService;
    private final BlacklistService blacklistService;
    private final UtilisateurService utilisateurService;
    private final MonitoringService monitoringService;

    public AdminController(@Autowired AuditLogService auditLogService, @Autowired BlacklistService blacklistService, @Autowired UtilisateurService utilisateurService, @Autowired MonitoringService monitoringService) {
        this.auditLogService = auditLogService;
        this.blacklistService = blacklistService;
        this.utilisateurService = utilisateurService;
        this.monitoringService = monitoringService;
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditLogDto>> audit() {
        return auditLogService.findAll().isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(auditLogService.findAll());
    }

    @GetMapping("/blacklist")
    public ResponseEntity<List<BlacklistDto>> blacklist() {
        return blacklistService.findAll().isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(blacklistService.findAll());
    }

    @GetMapping("/utilisateurs")
    public ResponseEntity<List<UtilisateurDto>> utilisateurs() {
        return utilisateurService.findAll().isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(utilisateurService.findAll());
    }

    @GetMapping("/monitoring")
    public ResponseEntity<MonitoringDto> getMonitoring() {
        MonitoringDto dto = monitoringService.getMonitoringData();
        return ResponseEntity.ok(dto);
    }
}
