package fr.thomasdindin.api_starter.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "blocked_ip")
@NoArgsConstructor
public class Blacklist {

    public Blacklist(String adresseIp,String raison) {
        this.adresseIp = adresseIp;
        this.raison = raison;
    }

    @Id
    @ColumnDefault("nextval('blocked_ip_id_seq')")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "adresse_ip", nullable = false, length = 50)
    private String adresseIp;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_blocage", nullable = false)
    private Instant dateBlocage;

    @Column(name = "raison", length = Integer.MAX_VALUE)
    private String raison;

}