package fr.thomasdindin.api_starter.authentication.utils;

import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.entities.utilisateur.enums.Role;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        // On instancie la classe à tester
        jwtUtils = new JwtUtils();
        // On injecte un SECRET_KEY valide
        ReflectionTestUtils.setField(jwtUtils, "SECRET_KEY", "Cette_Clé_est_assez_longue_pour_le_test_!12345");
        // On appelle manuellement la méthode init() pour générer la clé
        jwtUtils.init();

        // On construit un Utilisateur de test
        utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setEmail("john.doe@example.com");
        utilisateur.setRole(Role.CLIENT);
        utilisateur.setNom("Doe");
        utilisateur.setPrenom("John");
    }

    @Nested
    @DisplayName("Tests sur l'initialisation du JwtUtils")
    class InitTests {

        @Test
        @DisplayName("init() doit lever une exception si SECRET_KEY est null")
        void testInitWithNullSecretKey() {
            JwtUtils testJwtUtils = new JwtUtils();
            ReflectionTestUtils.setField(testJwtUtils, "SECRET_KEY", null);
            assertThrows(IllegalStateException.class, testJwtUtils::init);
        }

        @Test
        @DisplayName("init() doit lever une exception si SECRET_KEY est vide")
        void testInitWithEmptySecretKey() {
            JwtUtils testJwtUtils = new JwtUtils();
            ReflectionTestUtils.setField(testJwtUtils, "SECRET_KEY", "");
            assertThrows(IllegalStateException.class, testJwtUtils::init);
        }
    }

    @Nested
    @DisplayName("Tests sur la génération des tokens")
    class GenerationTests {

        @Test
        @DisplayName("generateToken() doit retourner un token non nul et valide")
        void testGenerateToken() {
            String token = jwtUtils.generateToken(utilisateur);

            assertNotNull(token, "Le token généré ne doit pas être null");
            assertTrue(jwtUtils.validateToken(token), "Le token généré doit être valide");
        }

        @Test
        @DisplayName("generateTokens() doit retourner un accessToken et un refreshToken valides")
        void testGenerateTokens() {
            Map<String, String> tokens = jwtUtils.generateTokens(utilisateur);

            assertNotNull(tokens.get("accessToken"), "accessToken ne doit pas être null");
            assertNotNull(tokens.get("refreshToken"), "refreshToken ne doit pas être null");

            assertTrue(jwtUtils.validateToken(tokens.get("accessToken")), "accessToken doit être valide");
            assertTrue(jwtUtils.validateToken(tokens.get("refreshToken")), "refreshToken doit être valide");
        }
    }

    @Nested
    @DisplayName("Tests sur la validation des tokens")
    class ValidationTests {

        @Test
        @DisplayName("validateToken() doit retourner false pour un token invalide")
        void testValidateTokenWithInvalidToken() {
            String invalidToken = "token_invalide_qui_ne_parse_pas_du_tout";
            assertFalse(jwtUtils.validateToken(invalidToken), "Un token invalide doit renvoyer false");
        }

        @Test
        @DisplayName("validateToken() doit retourner true pour un token valide")
        void testValidateTokenWithValidToken() {
            String token = jwtUtils.generateToken(utilisateur);
            assertTrue(jwtUtils.validateToken(token), "Un token valide doit renvoyer true");
        }
    }

    @Nested
    @DisplayName("Tests sur l'extraction du sujet")
    class ExtractionTests {

        @Test
        @DisplayName("extractSubject() doit retourner l'ID de l'utilisateur pour un token valide")
        void testExtractSubject() {
            String token = jwtUtils.generateToken(utilisateur);

            String subject = jwtUtils.extractSubject(token);
            assertEquals(utilisateur.getId().toString(), subject,
                    "Le subject du token doit correspondre à l'ID de l'utilisateur");
        }

        @Test
        @DisplayName("extractSubject() avec un token invalide doit lever une exception")
        void testExtractSubjectWithInvalidToken() {
            String invalidToken = "token_invalide_qui_ne_parse_pas_du_tout";

            // Ici, on s'attend à une exception, car extractSubject ne gère pas d'exception en interne
            // (elle va lever directement l'exception si le token est invalide).
            assertThrows(JwtException.class, () -> jwtUtils.extractSubject(invalidToken));
        }
    }
}
