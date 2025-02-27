package fr.thomasdindin.api_starter.authentication.service;

import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {
    @Value("${security.jwt.secret}")
    private String SECRET_KEY;
    private static final int JWT_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes
    private Key key;

    @PostConstruct
    public void init() {
        if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
            throw new IllegalStateException("SECRET_KEY is not configured properly.");
        }
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Génère un access token JWT pour un utilisateur.
     * Le token contient l'email, le rôle, le nom et le prénom de l'utilisateur.
     * Le sujet du token est l'id de l'utilisateur.
     * Le token expire après 15 minutes.
     * @param utilisateur l'utilisateur pour lequel on génère le token
     * @return le token JWT
     */
    public String generateAccessToken(Utilisateur utilisateur) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", utilisateur.getEmail());
        claims.put("role", utilisateur.getRole());
        claims.put("nom", utilisateur.getNom());
        claims.put("prenom", utilisateur.getPrenom());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(utilisateur.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Renvoie un refresh token qui a pour durée de vie une semaine.
     * Le sub est l'id de l'utilisateur.
     * @param utilisateur l'utilisateur pour lequel on génère le token
     * @return le refresh token
     */
    public String generateRefreshToken(Utilisateur utilisateur) {
        return Jwts.builder()
                .setSubject(utilisateur.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    // Valide un token JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Extrait le sujet (username/email) d'un JWT
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }
}
