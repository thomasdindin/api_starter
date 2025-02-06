package fr.thomasdindin.api_starter.mappers;

import fr.thomasdindin.api_starter.dto.UtilisateurDto;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;

public class UtilisateurMapper {
    public static UtilisateurDto toDto(Utilisateur utilisateur) {
        return UtilisateurDto.builder()
                .email(utilisateur.getEmail() == null ? "" : utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .dateNaissance(utilisateur.getDateNaissance())
                .dateCreation(utilisateur.getDateCreation())
                .genre(utilisateur.getGenre())
                .telephone(utilisateur.getTelephone())
                .photoProfil(utilisateur.getPhotoProfil())
                .build();
    }
}
