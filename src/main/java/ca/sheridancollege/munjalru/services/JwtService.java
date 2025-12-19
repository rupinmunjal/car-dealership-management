package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {
    private final String secretKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secretKey,
                      @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        if (expirationMs <= 0) {
            throw new IllegalArgumentException("app.jwt.expiration-ms must be positive");
        }
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    // ── username ─────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ── role ────────────────────────────────────────────────────

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // ── dealerId ────────────────────────────────────────────────

    public Long extractDealerId(String token) {
        Object raw = extractAllClaims(token).get("dealerId");
        if (raw instanceof Integer i) return i.longValue();
        if (raw instanceof Long l) return l;
        return null;
    }

    // ── permissions ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractClaim(token, claims ->
                (List<String>) claims.getOrDefault("permissions", Collections.emptyList()));
    }

    // ── generic claim extraction ────────────────────────────────

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ── token generation (generic UserDetails) ──────────────────

    public String generateToken(UserDetails userDetails) {
        if (userDetails instanceof User user) {
            return generateToken(user);
        }
        // Fallback for non-User UserDetails (should not happen in this app)
        return generateToken(new HashMap<>(), userDetails);
    }

    // ── token generation (User with role + dealerId + permissions)

    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        if (user.getDealer() != null) {
            extraClaims.put("dealerId", user.getDealer().getId());
        }
        if (user.getRole() == ca.sheridancollege.munjalru.beans.Role.DEALER_EMPLOYEE
                && user.getPermissions() != null) {
            List<String> permNames = user.getPermissions().stream()
                    .map(Enum::name)
                    .toList();
            extraClaims.put("permissions", permNames);
        }
        return generateToken(extraClaims, user);
    }

    // ── raw token generation ────────────────────────────────────

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    // ── validation ──────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ── parsing ─────────────────────────────────────────────────

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
