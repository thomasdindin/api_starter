package fr.thomasdindin.api_starter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BlacklistDto {
    private String adresseIp;
    private String raison;
    private Instant dateBlocage;
}
