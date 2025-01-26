package fr.thomasdindin.api_starter.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {
    private static final String SECRET_KEY = "your_secret_key_change_this_to_256_bit_key"; // Changez cette clé pour votre application
    private static final int JWT_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Génère un token JWT
    public String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Valide un token JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            e.printStackTrace(); // Vous pouvez ajouter des logs ici
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
