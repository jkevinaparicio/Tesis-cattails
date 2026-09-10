package backendWebProyectoApp.WebBackendApp.seguridad;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final Key SECRET_KEY;

    public JwtUtil(@Value("${app.jwt-secret}") String jwtSecret) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String correo, String role) {

        return Jwts.builder()
                .setSubject(correo) // usuario
                .claim("roles", List.of(role))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 día
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractCorreo(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        return getClaims(token).get("roles", List.class);
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}