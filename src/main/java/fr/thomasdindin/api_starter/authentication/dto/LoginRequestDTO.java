package fr.thomasdindin.api_starter.authentication.dto;

import fr.thomasdindin.api_starter.authentication.validators.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    @Email(message = "Email invalide")
    @NotNull(message = "L'email est obligatoire")
    private String email;

    @ValidPassword
    private String password;
}
