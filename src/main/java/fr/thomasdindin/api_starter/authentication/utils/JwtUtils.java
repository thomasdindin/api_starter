package fr.thomasdindin.api_starter.authentication.utils;

import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {
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

    // Génère un token JWT
    public String generateToken(Utilisateur utilisateur) {
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

    public Map<String, String> generateTokens(Utilisateur utilisateur) {
        String refreshToken = Jwts.builder()
                .setSubject(utilisateur.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 jours
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", generateToken(utilisateur));
        tokens.put("refreshToken", refreshToken);
        return tokens;
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
