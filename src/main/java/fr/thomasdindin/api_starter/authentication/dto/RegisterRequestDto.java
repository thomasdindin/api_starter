package fr.thomasdindin.api_starter.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequestDto {

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    /**
     * Mot de passe minimum 8 caractères,
     * doit contenir au moins une majuscule, un chiffre et un caractère spécial.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins une majuscule, un chiffre et un caractère spécial")
    private String password;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50)
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50)
    private String nom;
}
