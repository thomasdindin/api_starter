package fr.thomasdindin.api_starter.authentication.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String id;
    @Email
    private String email;
    private String token;
}
