package fr.thomasdindin.api_starter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class MonitoringDto {
    private Map<LocalDate, Long> loginsOkPerDay;
    private Map<LocalDate, Long> loginsKoPerDay;
    private Map<LocalDate, Long> inscriptionsPerDay;
    private Map<LocalDate, Long> blocagesPerDay;
}
