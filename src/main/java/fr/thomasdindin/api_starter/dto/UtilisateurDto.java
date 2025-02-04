package fr.thomasdindin.api_starter.dto;

import fr.thomasdindin.api_starter.entities.utilisateur.enums.GenreUtilisateur;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link Utilisateur}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UtilisateurDto implements Serializable {
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
    private Instant dateCreation;
    @NotNull
    @Size(max = 50)
    private String prenom;
    @NotNull
    @Size(max = 50)
    private String nom;
    @Size(max = 15)
    private String telephone;
    private Instant dateNaissance;
    private GenreUtilisateur genre;
    @Size(max = 255)
    private String photoProfil;
    private AdresseDto adresse;
}