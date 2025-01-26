package fr.thomasdindin.api_starter.audit.repository;

import fr.thomasdindin.api_starter.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}