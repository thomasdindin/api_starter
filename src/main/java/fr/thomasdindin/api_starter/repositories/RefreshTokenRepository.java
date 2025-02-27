package fr.thomasdindin.api_starter.repositories;

import fr.thomasdindin.api_starter.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findRefreshTokenByTokenHash(String tokenHash);
    Optional<RefreshToken> findByTokenHashAndUserId(String tokenHash, UUID userId);
    Optional<RefreshToken> findByTokenHash(String tokenHash);

}