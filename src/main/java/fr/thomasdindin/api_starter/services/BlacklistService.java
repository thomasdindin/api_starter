package fr.thomasdindin.api_starter.services;

import fr.thomasdindin.api_starter.audit.repository.AuditLogRepository;
import fr.thomasdindin.api_starter.dto.BlacklistDto;
import fr.thomasdindin.api_starter.entities.Blacklist;
import fr.thomasdindin.api_starter.mappers.BlacklistMapper;
import fr.thomasdindin.api_starter.repositories.BlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;
    private final AuditLogRepository auditLogRepository;

    public BlacklistService(@Autowired BlacklistRepository blacklistRepository, @Autowired AuditLogRepository auditLogRepository) {
        this.blacklistRepository = blacklistRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public boolean isBlacklisted(String adresseIp) {
        return blacklistRepository.findByAdresseIp(adresseIp).isPresent();
    }

    public void blockIp(String adresseIp, String raison) {
        blacklistRepository.save(new Blacklist(adresseIp, raison));
    }

    public void analyzeAndBlockSuspectIps(int maxRequests, int timeWindowInMinutes) {
        Instant startTime = Instant.now().minus(timeWindowInMinutes, ChronoUnit.MINUTES);

        // Group logs by IP and count the requests
        List<Object[]> suspectIps = auditLogRepository.findSuspectIps(startTime, maxRequests);

        for (Object[] result : suspectIps) {
            String ipAddress = (String) result[0];
            Long requestCount = (Long) result[1];

            // Bloquer les IP si elles dépassent la limite
            blockIp(ipAddress, "Dépassement des " + maxRequests + " requêtes en " + timeWindowInMinutes + " minutes.");
        }
    }

    public List<BlacklistDto> findAll() {
        return blacklistRepository.findAll().stream().map(BlacklistMapper::toDto).toList();
    }
}
