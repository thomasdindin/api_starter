package fr.thomasdindin.api_starter.authentication.service;

import fr.thomasdindin.api_starter.entities.RefreshToken;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.repositories.RefreshTokenRepository;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UtilisateurRepository utilisateurRepository,
                               JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.jwtService = jwtService;
    }

    /**
     * Crée un nouveau refreshToken pour un utilisateur donné.
     *
     * @param utilisateur L'utilisateur pour lequel générer un refreshToken.
     * @param request La requête HTTP permettant d'extraire l'IP et le User-Agent.
     */
    @Transactional
    public void createRefreshToken(String token, Utilisateur utilisateur, HttpServletRequest request) {
        // Calculer le hash du refreshToken
        String tokenHash = DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));

        // Enregistrer le refreshToken en base
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setTokenHash(tokenHash);
        refreshTokenEntity.setUser(utilisateur);
        refreshTokenEntity.setIpAddress(request.getRemoteAddr());
        refreshTokenEntity.setDeviceInfo(request.getHeader("User-Agent"));
        refreshTokenEntity.setLastUsed(Instant.now());
        refreshTokenRepository.save(refreshTokenEntity);
    }

    /**
     * Vérifie le refreshToken et, si toutes les conditions sont réunies, génère un nouvel access token.
     *
     * @param refreshTokenStr Le refreshToken (JWT) envoyé par le client.
     * @param request La requête HTTP permettant d'extraire l'IP et le User-Agent.
     * @return Un nouvel access token JWT.
     * @throws JwtException Si le refreshToken est invalide, introuvable ou si les informations d'IP / User-Agent ne correspondent pas.
     */
    @Transactional
    public String refreshAccessToken(String refreshTokenStr, HttpServletRequest request) {
        // 1. Valider le refreshToken (signature, expiration, etc.)
        if (!jwtService.validateToken(refreshTokenStr)) {
            throw new JwtException("Refresh token invalide");
        }

        // 2. Extraire le subject (user id) du refreshToken
        String userIdStr = jwtService.extractSubject(refreshTokenStr);
        UUID userId = UUID.fromString(userIdStr);

        // 3. Calculer le hash du refreshToken pour le comparer à la valeur stockée en base
        // Ici, nous utilisons MD5 pour l'exemple (pour une sécurité accrue, pensez à SHA-256)
        String tokenHash = DigestUtils.md5DigestAsHex(refreshTokenStr.getBytes(StandardCharsets.UTF_8));

        // 4. Récupérer l'enregistrement en base pour cet utilisateur et ce token
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByTokenHashAndUserId(tokenHash, userId);
        if (optionalRefreshToken.isEmpty()) {
            throw new JwtException("Refresh token non trouvé en base");
        }
        RefreshToken storedToken = optionalRefreshToken.get();

        // 5. Vérifier que le token n'a pas été révoqué
        if (storedToken.isRevoked()) {
            throw new JwtException("Refresh token révoqué");
        }

        // 6. Récupérer l'IP et le User-Agent depuis la requête
        String requestIp = request.getRemoteAddr();
        String requestUserAgent = request.getHeader("User-Agent");

        // Vérifier que l'IP et le User-Agent correspondent à ceux enregistrés
        if (!storedToken.getIpAddress().equals(requestIp) || !storedToken.getDeviceInfo().equals(requestUserAgent)) {
            throw new JwtException("L'IP ou le User-Agent ne correspondent pas");
        }

        // 7. Mettre à jour le timestamp de dernière utilisation
        storedToken.setLastUsed(Instant.now());
        refreshTokenRepository.save(storedToken);

        // 8. Récupérer l'utilisateur et générer un nouvel access token
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new JwtException("Utilisateur non trouvé"));
        return jwtService.generateAccessToken(utilisateur);
    }

    /**
     * Révoque un refreshToken en base de données.
     *
     * @param refreshToken
     */
    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        // Calculer le hash du refreshToken
        String tokenHash = DigestUtils.md5DigestAsHex(refreshToken.getBytes(StandardCharsets.UTF_8));

        // Rechercher l'enregistrement en base
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByTokenHash(tokenHash);
        if (optionalRefreshToken.isPresent()) {
            RefreshToken storedToken = optionalRefreshToken.get();
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
        } else {
            throw new JwtException("Refresh token non trouvé en base");
        }
    }
}
