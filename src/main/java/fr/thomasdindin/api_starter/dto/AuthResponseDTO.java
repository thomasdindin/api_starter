package fr.thomasdindin.api_starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String id;
    private String email;
    private String token;
}
