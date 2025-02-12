package fr.thomasdindin.api_starter.entities.utilisateur;

import fr.thomasdindin.api_starter.entities.Adresse;
import fr.thomasdindin.api_starter.entities.utilisateur.enums.GenreUtilisateur;
import fr.thomasdindin.api_starter.entities.utilisateur.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50)
    private Role role;


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

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'Inconnu'")
    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'Inconnu'")
    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Size(max = 15)
    @Column(name = "telephone", length = 15)
    private String telephone;

    @Column(name = "date_naissance")
    private Instant dateNaissance;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre", length = 10)
    private GenreUtilisateur genre;

    @Size(max = 255)
    @Column(name = "photo_profil")
    private String photoProfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "adresse_id")
    private Adresse adresse;

    @PrePersist
    protected void onCreate() {
        dateCreation = Instant.now();
        compteActive = false;
        compteBloque = false;
        role = Role.CLIENT;
    }

}