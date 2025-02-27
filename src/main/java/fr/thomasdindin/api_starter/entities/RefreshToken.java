package fr.thomasdindin.api_starter.entities;

import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur user;

    @NotNull
    @Column(name = "token_hash", nullable = false, length = Integer.MAX_VALUE)
    private String tokenHash;

    @NotNull
    @Column(name = "device_info", nullable = false, length = Integer.MAX_VALUE)
    private String deviceInfo;

    @Column(name = "ip_address", length = Integer.MAX_VALUE)
    private String ipAddress;

    @ColumnDefault("now()")
    @Column(name = "last_used")
    private Instant lastUsed;

    @ColumnDefault("false")
    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID();
    }

}