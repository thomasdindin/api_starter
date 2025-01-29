package fr.thomasdindin.api_starter.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "adresse")
public class Adresse {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 10)
    @NotNull
    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    @Size(max = 255)
    @NotNull
    @Column(name = "rue", nullable = false)
    private String rue;

    @Size(max = 100)
    @NotNull
    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

    @Size(max = 20)
    @NotNull
    @Column(name = "code_postal", nullable = false, length = 20)
    private String codePostal;

    @Size(max = 100)
    @NotNull
    @Column(name = "pays", nullable = false, length = 100)
    private String pays;

    @Size(max = 255)
    @Column(name = "complement")
    private String complement;

}