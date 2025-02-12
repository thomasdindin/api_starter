package fr.thomasdindin.api_starter.authentication.service;

import fr.thomasdindin.api_starter.audit.AuditAction;
import fr.thomasdindin.api_starter.audit.service.AuditLogService;
import fr.thomasdindin.api_starter.authentication.dto.RegisterRequestDto;
import fr.thomasdindin.api_starter.authentication.errors.AccountBlockedException;
import fr.thomasdindin.api_starter.authentication.errors.AuthenticationException;
import fr.thomasdindin.api_starter.authentication.errors.EmailNotVerfiedException;
import fr.thomasdindin.api_starter.authentication.errors.NoMatchException;
import fr.thomasdindin.api_starter.authentication.utils.JwtUtils;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<Utilisateur> utilisateurCaptor;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Par défaut, on mock le RemoteAddr
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Nested
    @DisplayName("Tests de registerUtilisateur")
    class RegisterUtilisateurTests {

        @Test
        @DisplayName("Doit créer un nouvel utilisateur si email inexistant")
        void testRegisterUtilisateur_Success() {
            RegisterRequestDto dto = new RegisterRequestDto();
            dto.setEmail("new@example.com");
            dto.setPassword("Password@1");
            dto.setPrenom("John");
            dto.setNom("Doe");

            // On simulera que l'utilisateur n'existe pas
            when(utilisateurRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

            Utilisateur saved = new Utilisateur();
            saved.setId(UUID.randomUUID());
            saved.setEmail(dto.getEmail());

            when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(saved);

            Utilisateur result = authenticationService.registerUtilisateur(dto, request);

            // Vérifications
            verify(auditLogService).log(eq(AuditAction.SUCCESSFUL_REGISTRATION), eq(saved), eq("127.0.0.1"));
            assertNotNull(result);
            assertEquals(dto.getEmail(), result.getEmail());
        }

        @Test
        @DisplayName("Doit lever une AuthenticationException si l'email existe déjà")
        void testRegisterUtilisateur_EmailExists() {
            RegisterRequestDto dto = new RegisterRequestDto();
            dto.setEmail("existing@example.com");
            dto.setPassword("Password@1");
            dto.setPrenom("John");
            dto.setNom("Doe");

            Utilisateur existingUser = new Utilisateur();
            existingUser.setEmail(dto.getEmail());

            when(utilisateurRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));

            assertThrows(AuthenticationException.class, () ->
                    authenticationService.registerUtilisateur(dto, request));

            // On vérifie que le log FAILED_REGISTRATION a été fait
            verify(auditLogService).log(eq(AuditAction.FAILED_REGISTRATION), eq(existingUser), eq("127.0.0.1"));
            // On s'assure qu'aucune sauvegarde n'a eu lieu
            verify(utilisateurRepository, never()).save(any(Utilisateur.class));
        }
    }

    @Nested
    @DisplayName("Tests de authenticate")
    class AuthenticateTests {

        private Utilisateur utilisateur;

        @BeforeEach
        void initUser() {
            utilisateur = new Utilisateur();
            utilisateur.setId(UUID.randomUUID());
            utilisateur.setEmail("john@example.com");
            utilisateur.setMotDePasse(passwordEncoder.encode("Password@1"));
            utilisateur.setCompteBloque(false);
            utilisateur.setCompteActive(true);
            utilisateur.setTentativesConnexion((short) 0);
        }

        @Test
        @DisplayName("Doit lever une AuthenticationException si l'utilisateur n'existe pas")
        void testAuthenticate_UserNotFound() {
            when(utilisateurRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(AuthenticationException.class, () ->
                    authenticationService.authenticate("unknown@example.com", "Password@1", request));

            // On vérifie le log
            verify(auditLogService).log(eq(AuditAction.FAILED_LOGIN), isNull(), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Doit lever AccountBlockedException si le compte est bloqué")
        void testAuthenticate_UserBlocked() {
            utilisateur.setCompteBloque(true);
            when(utilisateurRepository.findByEmail(utilisateur.getEmail())).thenReturn(Optional.of(utilisateur));

            assertThrows(AccountBlockedException.class, () ->
                    authenticationService.authenticate(utilisateur.getEmail(), "Password@1", request));

            verify(auditLogService).log(eq(AuditAction.BLOCKED_ACCOUNT), eq(utilisateur), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Doit lever AuthenticationException si le mot de passe est incorrect")
        void testAuthenticate_BadPassword() {
            when(utilisateurRepository.findByEmail(utilisateur.getEmail())).thenReturn(Optional.of(utilisateur));

            // Utiliser un mot de passe erroné
            assertThrows(AuthenticationException.class, () ->
                    authenticationService.authenticate(utilisateur.getEmail(), "WrongPassword", request));

            // On vérifie que logginError() a incrémenté la tentative
            assertEquals(1, utilisateur.getTentativesConnexion().intValue());
            verify(auditLogService).log(eq(AuditAction.FAILED_LOGIN), eq(utilisateur), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Doit lever EmailNotVerfiedException si le compte n'est pas activé")
        void testAuthenticate_EmailNotVerified() {
            utilisateur.setCompteActive(false);
            when(utilisateurRepository.findByEmail(utilisateur.getEmail())).thenReturn(Optional.of(utilisateur));

            assertThrows(EmailNotVerfiedException.class, () ->
                    authenticationService.authenticate(utilisateur.getEmail(), "Password@1", request));

            verify(auditLogService).log(eq(AuditAction.FAILED_LOGIN), eq(utilisateur), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Doit renvoyer les tokens et reset tentatives si OK")
        void testAuthenticate_Success() {
            when(utilisateurRepository.findByEmail(utilisateur.getEmail())).thenReturn(Optional.of(utilisateur));

            // Mock la génération de tokens
            Map<String, String> tokensMap = new HashMap<>();
            tokensMap.put("accessToken", "fakeAccess");
            tokensMap.put("refreshToken", "fakeRefresh");
            when(jwtUtils.generateTokens(utilisateur)).thenReturn(tokensMap);

            Map<String, String> result = authenticationService.authenticate(
                    utilisateur.getEmail(), "Password@1", request);

            assertNotNull(result);
            assertEquals("fakeAccess", result.get("accessToken"));
            assertEquals("fakeRefresh", result.get("refreshToken"));

            // Vérifier que la tentative est reset
            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            Utilisateur saved = utilisateurCaptor.getValue();
            assertEquals(0, saved.getTentativesConnexion().intValue());

            verify(auditLogService).log(eq(AuditAction.SUCCESSFUL_LOGIN), eq(utilisateur), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Doit bloquer le compte après 3 tentatives invalides")
        void testAuthenticate_ThreeInvalidAttempts() {
            when(utilisateurRepository.findByEmail(utilisateur.getEmail()))
                    .thenReturn(Optional.of(utilisateur));

            // 1ère tentative échouée
            assertThrows(AuthenticationException.class, () ->
                    authenticationService.authenticate(utilisateur.getEmail(), "WrongPassword", request));
            assertEquals(1, utilisateur.getTentativesConnexion().intValue());

            // 2ème tentative échouée
            assertThrows(AuthenticationException.class, () ->
                    authenticationService.authenticate(utilisateur.getEmail(), "WrongPassword", request));
            assertEquals(2, utilisateur.getTentativesConnexion().intValue());

            // 3ème tentative échouée -> blocage
            AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                    authenticationService.authenticate(utilisateur.getEmail(), "WrongPassword", request));
            assertEquals("Votre compte a été bloqué suite à trop de tentatives échouées.", ex.getMessage());
            assertEquals(3, utilisateur.getTentativesConnexion().intValue());
            assertTrue(utilisateur.getCompteBloque());

            verify(auditLogService, times(1))
                    .log(eq(AuditAction.BLOCKED_ACCOUNT), eq(utilisateur), eq("127.0.0.1"));
        }
    }

    @Nested
    @DisplayName("Tests de verifyEmail")
    class VerifyEmailTests {

        @Test
        @DisplayName("Doit lever NoMatchException si l'utilisateur n'existe pas")
        void testVerifyEmail_UserNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(utilisateurRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThrows(NoMatchException.class, () ->
                    authenticationService.verifyEmail(unknownId, request));

            verify(auditLogService).log(eq(AuditAction.FAILED_EMAIL_VERIFICATION), isNull(), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Doit lever UnsupportedOperationException si le compte est déjà activé")
        void testVerifyEmail_AlreadyActive() {
            UUID userId = UUID.randomUUID();
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(userId);
            utilisateur.setCompteActive(true);

            when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(utilisateur));

            assertThrows(UnsupportedOperationException.class, () ->
                    authenticationService.verifyEmail(userId, request));

            verify(auditLogService).log(eq(AuditAction.FAILED_EMAIL_VERIFICATION), eq(utilisateur), eq("127.0.0.1"));
        }

        @Test
        @DisplayName("Doit activer le compte si OK")
        void testVerifyEmail_Success() {
            UUID userId = UUID.randomUUID();
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(userId);
            utilisateur.setCompteActive(false);

            when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(utilisateur));

            Utilisateur result = authenticationService.verifyEmail(userId, request);

            assertTrue(result.getCompteActive());
            verify(auditLogService).log(eq(AuditAction.SUCCESSFUL_EMAIL_VERIFICATION), eq(utilisateur), eq("127.0.0.1"));
            verify(utilisateurRepository).save(utilisateur);
        }
    }

    @Nested
    @DisplayName("Tests de refreshToken")
    class RefreshTokenTests {

        @Test
        @DisplayName("Doit lever NoMatchException si l'utilisateur n'existe pas")
        void testRefreshToken_UserNotFound() {
            // On fait semblant de parser un userId = random
            UUID userId = UUID.randomUUID();
            when(jwtUtils.extractSubject("fakeRefresh")).thenReturn(userId.toString());
            when(utilisateurRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NoMatchException.class, () -> authenticationService.refreshToken("fakeRefresh"));
        }

        @Test
        @DisplayName("Doit renvoyer un nouveau token si OK")
        void testRefreshToken_Success() {
            // On fait semblant de parser un userId = random
            UUID userId = UUID.randomUUID();
            when(jwtUtils.extractSubject("fakeRefresh")).thenReturn(userId.toString());

            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(userId);

            when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(utilisateur));

            when(jwtUtils.generateToken(utilisateur)).thenReturn("newAccessToken");

            String result = authenticationService.refreshToken("fakeRefresh");
            assertEquals("newAccessToken", result);
        }
    }
}
