package fr.thomasdindin.api_starter.services;

import fr.thomasdindin.api_starter.authentication.service.AuthenticationService;
import fr.thomasdindin.api_starter.entities.Utilisateur;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void testRegisterUtilisateur_Success() {
//        UUID utilisateurId = UUID.randomUUID(); // Utilisation de UUID
//
//        Utilisateur utilisateur = new Utilisateur();
//        utilisateur.setId(utilisateurId); // Définir l'ID UUID pour l'utilisateur
//        utilisateur.setEmail("test@example.com");
//        utilisateur.setMotDePasse("password123");
//
//        Mockito.when(utilisateurRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.empty());
//
//        Utilisateur savedUtilisateur = new Utilisateur();
//        savedUtilisateur.setId(utilisateurId); // UUID pour le résultat sauvegardé
//        savedUtilisateur.setEmail("test@example.com");
//        savedUtilisateur.setMotDePasse("hashedPassword");
//
//        Mockito.when(utilisateurRepository.save(Mockito.any(Utilisateur.class)))
//                .thenReturn(savedUtilisateur);
//
//        Utilisateur result = authenticationService.registerUtilisateur(utilisateur, any(HttpServletRequest.class));
//
//        Assertions.assertNotNull(result);
//        Assertions.assertEquals(utilisateurId, result.getId()); // Validation de l'UUID
//        Assertions.assertEquals("test@example.com", result.getEmail());
//        Assertions.assertEquals("hashedPassword", result.getMotDePasse());
//
//        Mockito.verify(utilisateurRepository).findByEmail("test@example.com");
//        Mockito.verify(utilisateurRepository).save(Mockito.any(Utilisateur.class));
    }

    @Test
    public void testRegisterUtilisateur_Failure() {
//        UUID utilisateurId = UUID.randomUUID();
//        Utilisateur utilisateur = new Utilisateur();
//        utilisateur.setId(utilisateurId);
//        utilisateur.setEmail("test@example.com");
//        utilisateur.setMotDePasse("password123");
//
//        Mockito.when(utilisateurRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.of(utilisateur));
//
//        Assertions.assertThrows(IllegalArgumentException.class, () -> {
//            authenticationService.registerUtilisateur(utilisateur);
//        });
    }
}

