package fr.thomasdindin.api_starter.audit.repository;

import fr.thomasdindin.api_starter.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    @Query("SELECT al.adresseIp, COUNT(al.adresseIp) AS requestCount " +
            "FROM AuditLog al " +
            "WHERE al.dateAction >= :startTime " +
            "GROUP BY al.adresseIp " +
            "HAVING COUNT(al.adresseIp) > :maxRequests")
    List<Object[]> findSuspectIps(Instant startTime, int maxRequests);
}