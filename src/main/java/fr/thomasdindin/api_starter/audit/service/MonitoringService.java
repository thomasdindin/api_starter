package fr.thomasdindin.api_starter.audit.service;

import fr.thomasdindin.api_starter.audit.AuditAction;
import fr.thomasdindin.api_starter.audit.AuditLog;
import fr.thomasdindin.api_starter.audit.repository.AuditLogRepository;
import fr.thomasdindin.api_starter.dto.MonitoringDto;
import fr.thomasdindin.api_starter.entities.Blacklist;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.repositories.BlacklistRepository;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MonitoringService {

    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private BlacklistRepository blacklistRepository;

    public MonitoringDto getMonitoringData() {
        LocalDate earliest = findEarliestDate();
        LocalDate latest = LocalDate.now();

        // 1) Récupérer les compteurs bruts
        Map<LocalDate, Long> loginsOkRaw = findLoginsOkByDay();
        Map<LocalDate, Long> loginsKoRaw = findLoginsKoByDay();
        Map<LocalDate, Long> inscriptionsRaw = findInscriptionsByDay();
        Map<LocalDate, Long> blocagesRaw = findBlocagesByDay();

        // 2) Compléter les jours vides
        Map<LocalDate, Long> loginsOkFilled = fillMissingDays(earliest, latest, loginsOkRaw);
        Map<LocalDate, Long> loginsKoFilled = fillMissingDays(earliest, latest, loginsKoRaw);
        Map<LocalDate, Long> inscriptionsFilled = fillMissingDays(earliest, latest, inscriptionsRaw);
        Map<LocalDate, Long> blocagesFilled = fillMissingDays(earliest, latest, blocagesRaw);

        // 3) On crée un DTO avec ces 4 maps (converties en String, ou restées en LocalDate)
        return MonitoringDto.builder()
                .loginsOkPerDay(loginsOkFilled)
                .loginsKoPerDay(loginsKoFilled)
                .inscriptionsPerDay(inscriptionsFilled)
                .blocagesPerDay(blocagesFilled)
                .build();
    }


    public LocalDate findEarliestDate() {
        LocalDate earliestAudit = auditLogRepository.findMinDateAction() // retourne un Instant
                .map(i -> i.atZone(ZoneId.systemDefault()).toLocalDate())
                .orElse(LocalDate.now());

        LocalDate earliestUser = utilisateurRepository.findMinDateCreation()
                .map(i -> i.atZone(ZoneId.systemDefault()).toLocalDate())
                .orElse(LocalDate.now());

        LocalDate earliestBlacklist = blacklistRepository.findMinDateBlocage()
                .map(i -> i.atZone(ZoneId.systemDefault()).toLocalDate())
                .orElse(LocalDate.now());

        // On prend la plus ancienne des trois
        return Stream.of(earliestAudit, earliestUser, earliestBlacklist)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
    }

    public Map<LocalDate, Long> fillMissingDays(LocalDate start, LocalDate end, Map<LocalDate, Long> rawData) {
        Map<LocalDate, Long> result = new LinkedHashMap<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            long count = rawData.getOrDefault(current, 0L);
            result.put(current, count);
            current = current.plusDays(1);
        }
        return result;
    }


    public Map<LocalDate, Long> findLoginsOkByDay() {
        // On récupère tous les logs d’audit dont l’action est LOGIN_OK
        List<AuditLog> logs = auditLogRepository.findByAction(AuditAction.SUCCESSFUL_LOGIN);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> toLocalDate(log.getDateAction()),
                        Collectors.counting()
                ));
    }

    public Map<LocalDate, Long> findLoginsKoByDay() {
        // On récupère tous les logs d’audit dont l’action est LOGIN_FAILED
        List<AuditLog> logs = auditLogRepository.findByAction(AuditAction.FAILED_LOGIN);

        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> toLocalDate(log.getDateAction()),
                        Collectors.counting()
                ));
    }


    public Map<LocalDate, Long> findInscriptionsByDay() {
        List<Utilisateur> users = utilisateurRepository.findAll();

        return users.stream()
                .collect(Collectors.groupingBy(
                        u -> toLocalDate(u.getDateCreation()),
                        Collectors.counting()
                ));
    }


    public Map<LocalDate, Long> findBlocagesByDay() {
        List<Blacklist> blocs = blacklistRepository.findAll();

        return blocs.stream()
                .collect(Collectors.groupingBy(
                        b -> toLocalDate(b.getDateBlocage()),
                        Collectors.counting()
                ));
    }

    // Petit helper pour convertir un Instant en LocalDate
    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

}

