package fr.thomasdindin.api_starter.repositories;

import fr.thomasdindin.api_starter.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}