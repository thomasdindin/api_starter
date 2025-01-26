package fr.thomasdindin.api_starter.services;

import fr.thomasdindin.api_starter.dto.AuthResponseDTO;
import fr.thomasdindin.api_starter.entities.Utilisateur;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import fr.thomasdindin.api_starter.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UtilisateurService(@Autowired UtilisateurRepository utilisateurRepository, @Autowired JwtUtils jwtUtils) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtils = jwtUtils;
    }

    /**
     * Inscription d'un nouvel utilisateur
     */
    public Utilisateur registerUtilisateur(Utilisateur utilisateur) {
        // Vérifier si l'utilisateur existe déjà
        Optional<Utilisateur> existingUser = utilisateurRepository.findByEmail(utilisateur.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet e-mail existe déjà.");
        }

        // Hacher le mot de passe
        String encodedPassword = passwordEncoder.encode(utilisateur.getMotDePasse());
        utilisateur.setMotDePasse(encodedPassword);

        return utilisateurRepository.save(utilisateur);
    }

    public AuthResponseDTO authenticate(String email, String motDePasse) {
        // Récupérer l'utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Mot de passe incorrect");
        }

        // Générer un JWT
        String token = jwtUtils.generateToken(utilisateur.getEmail());

        // Renvoyer les données sous forme de DTO
        return new AuthResponseDTO(
                utilisateur.getId().toString(),
                utilisateur.getEmail(),
                token
        );
    }
}
