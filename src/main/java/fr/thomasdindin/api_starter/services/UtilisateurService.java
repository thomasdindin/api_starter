package fr.thomasdindin.api_starter.services;

import fr.thomasdindin.api_starter.dto.UtilisateurDto;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(@Autowired UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public UtilisateurDto findByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElseThrow();
        return new UtilisateurDto(
                utilisateur.getEmail(),
                utilisateur.getDateCreation(),
                utilisateur.getPrenom(),
                utilisateur.getNom(),
                utilisateur.getTelephone(),
                utilisateur.getDateNaissance(),
                utilisateur.getGenre(),
                utilisateur.getPhotoProfil(),
                null
        );
    }

    public UtilisateurDto findById(UUID id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElseThrow();
        return new UtilisateurDto(
                utilisateur.getEmail(),
                utilisateur.getDateCreation(),
                utilisateur.getPrenom(),
                utilisateur.getNom(),
                utilisateur.getTelephone(),
                utilisateur.getDateNaissance(),
                utilisateur.getGenre(),
                utilisateur.getPhotoProfil(),
                null
        );
    }

}
