package fr.thomasdindin.api_starter.entities;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link RefreshToken}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class RefreshTokenDto implements Serializable {
    private UUID id;
    @NotNull
    private String tokenHash;
    @NotNull
    private String deviceInfo;
    private String ipAddress;
    private Instant lastUsed;
    private boolean revoked;
}