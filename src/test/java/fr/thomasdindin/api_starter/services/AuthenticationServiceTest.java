package fr.thomasdindin.api_starter.services;

import fr.thomasdindin.api_starter.audit.AuditAction;
import fr.thomasdindin.api_starter.audit.service.AuditLogService;
import fr.thomasdindin.api_starter.authentication.dto.AuthResponseDTO;
import fr.thomasdindin.api_starter.authentication.errors.AccountBlockedException;
import fr.thomasdindin.api_starter.authentication.errors.AuthenticationException;
import fr.thomasdindin.api_starter.authentication.errors.EmailNotVerfiedException;
import fr.thomasdindin.api_starter.authentication.errors.NoMatchException;
import fr.thomasdindin.api_starter.authentication.service.AuthenticationService;
import fr.thomasdindin.api_starter.authentication.utils.JwtUtils;
import fr.thomasdindin.api_starter.entities.Utilisateur;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void testRegisterUtilisateur_successful() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@example.com");
        utilisateur.setMotDePasse("ValidPassword123!");

        when(utilisateurRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilisateur savedUtilisateur = authenticationService.registerUtilisateur(utilisateur, httpServletRequest);

        assertNotNull(savedUtilisateur);
        assertNotNull(savedUtilisateur.getMotDePasse());
        verify(auditLogService).log(eq(AuditAction.SUCCESSFUL_REGISTRATION), any(Utilisateur.class), eq("127.0.0.1"));
    }

    @Test
    void testRegisterUtilisateur_failure_userAlreadyExists() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@example.com");
        utilisateur.setMotDePasse("ValidPassword123!");

        when(utilisateurRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new Utilisateur()));

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.registerUtilisateur(utilisateur, httpServletRequest));

        assertEquals("Un utilisateur avec cet e-mail existe déjà.", exception.getMessage());
        verify(auditLogService).log(eq(AuditAction.FAILED_REGISTRATION), any(Utilisateur.class), eq("127.0.0.1"));
    }

    @Test
    void testAuthenticate_successful() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setEmail("test@example.com");
        utilisateur.setMotDePasse(new BCryptPasswordEncoder().encode("ValidPassword123!"));
        utilisateur.setCompteActive(true);
        utilisateur.setCompteBloque(false);

        when(utilisateurRepository.findByEmail("test@example.com")).thenReturn(Optional.of(utilisateur));
        when(jwtUtils.generateToken("test@example.com")).thenReturn("mockJwtToken");

        AuthResponseDTO response = authenticationService.authenticate("test@example.com", "ValidPassword123!", httpServletRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("mockJwtToken", response.getToken());
        verify(auditLogService).log(eq(AuditAction.SUCCESSFUL_LOGIN), any(Utilisateur.class), eq("127.0.0.1"));
    }

    @Test
    void testAuthenticate_failure_accountBlocked() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@example.com");
        utilisateur.setCompteBloque(true);

        when(utilisateurRepository.findByEmail("test@example.com")).thenReturn(Optional.of(utilisateur));

        AccountBlockedException exception = assertThrows(AccountBlockedException.class,
                () -> authenticationService.authenticate("test@example.com", "ValidPassword123!", httpServletRequest));

        assertEquals("Votre compte est temporairement bloqué suite à des tentatives échouées.", exception.getMessage());
        verify(auditLogService).log(eq(AuditAction.BLOCKED_ACCOUNT), any(Utilisateur.class), eq("127.0.0.1"));
    }

    @Test
    void testAuthenticate_failure_invalidPassword() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@example.com");
        utilisateur.setMotDePasse(new BCryptPasswordEncoder().encode("CorrectPassword123!"));
        utilisateur.setCompteActive(true);
        utilisateur.setCompteBloque(false);

        when(utilisateurRepository.findByEmail("test@example.com")).thenReturn(Optional.of(utilisateur));

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("test@example.com", "WrongPassword123!", httpServletRequest));

        assertEquals("Mot de passe incorrect", exception.getMessage());
        verify(auditLogService).log(eq(AuditAction.FAILED_LOGIN), any(Utilisateur.class), eq("127.0.0.1"));
    }

    @Test
    void testVerifyEmail_successful() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setEmail("test@example.com");
        utilisateur.setCompteActive(false);

        when(utilisateurRepository.findById(any(UUID.class))).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        Utilisateur verifiedUtilisateur = authenticationService.verifyEmail(utilisateur.getId(), httpServletRequest);

        assertTrue(verifiedUtilisateur.getCompteActive());
        verify(auditLogService).log(eq(AuditAction.SUCCESSFUL_EMAIL_VERIFICATION), any(Utilisateur.class), eq("127.0.0.1"));
    }

    @Test
    void testVerifyEmail_failure_userNotFound() {
        when(utilisateurRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        NoMatchException exception = assertThrows(NoMatchException.class,
                () -> authenticationService.verifyEmail(UUID.randomUUID(), httpServletRequest));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(auditLogService).log(eq(AuditAction.FAILED_EMAIL_VERIFICATION), eq(null), eq("127.0.0.1"));
    }

    @Test
    void testVerifyEmail_failure_accountAlreadyActive() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setCompteActive(true);

        when(utilisateurRepository.findById(any(UUID.class))).thenReturn(Optional.of(utilisateur));

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> authenticationService.verifyEmail(UUID.randomUUID(), httpServletRequest));

        assertEquals("Votre compte est déjà activé", exception.getMessage());
        verify(auditLogService).log(eq(AuditAction.FAILED_EMAIL_VERIFICATION), any(Utilisateur.class), eq("127.0.0.1"));
    }
}
