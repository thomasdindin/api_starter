package fr.thomasdindin.api_starter.mappers;

import fr.thomasdindin.api_starter.dto.BlacklistDto;
import fr.thomasdindin.api_starter.entities.Blacklist;

public class BlacklistMapper {
    public static BlacklistDto toDto(Blacklist blacklist) {
        return BlacklistDto.builder()
                .adresseIp(blacklist.getAdresseIp())
                .dateBlocage(blacklist.getDateBlocage())
                .build();
    }
}
