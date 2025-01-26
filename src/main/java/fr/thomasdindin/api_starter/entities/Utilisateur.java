package fr.thomasdindin.api_starter.entities;

import jakarta.persistence.*;
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

    @Column(name = "email", nullable = false)
    private String email;

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

}