package fr.thomasdindin.api_starter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link fr.thomasdindin.api_starter.entities.Adresse}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AdresseDto implements Serializable {
    private UUID id;
    @NotNull
    @Size(max = 10)
    private String numero;
    @NotNull
    @Size(max = 255)
    private String rue;
    @NotNull
    @Size(max = 100)
    private String ville;
    @NotNull
    @Size(max = 20)
    private String codePostal;
    @NotNull
    @Size(max = 100)
    private String pays;
    @Size(max = 255)
    private String complement;
}