package fr.thomasdindin.api_starter.services;

import fr.thomasdindin.api_starter.dto.UtilisateurDto;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.mappers.UtilisateurMapper;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(@Autowired UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public UtilisateurDto findByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElseThrow();
        return UtilisateurMapper.toDto(utilisateur);
    }

    public UtilisateurDto findById(UUID id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElseThrow();
        return UtilisateurMapper.toDto(utilisateur);
    }

    public List<UtilisateurDto> findAll() {
        return utilisateurRepository.findAll().stream().map(
                UtilisateurMapper::toDto
        ).toList();
    }

}
