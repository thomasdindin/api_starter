package fr.thomasdindin.api_starter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "utilisateur")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins une majuscule, un chiffre et un caractère spécial")
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @ColumnDefault("'USER'")
    @Column(name = "role", length = 50)
    private String role;

    @ColumnDefault("false")
    @Column(name = "compte_active")
    private Boolean compteActive;

    @ColumnDefault("false")
    @Column(name = "compte_bloque")
    private Boolean compteBloque;

    @ColumnDefault("0")
    @Column(name = "tentatives_connexion")
    private Short tentativesConnexion;

    @ColumnDefault("now()")
    @Column(name = "date_creation")
    private Instant dateCreation;

    @PrePersist
    protected void onCreate() {
        dateCreation = Instant.now();
    }

}