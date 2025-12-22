package ca.sheridancollege.munjalru.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString("a-very-strong-secret-key-that-is-at-least-64-bytes-long-for-hs256!".getBytes());
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        UserDetails userDetails = User.withUsername("user@example.com")
                .password("password")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void shouldRejectInvalidToken() {
        UserDetails userDetails = User.withUsername("user@example.com")
                .password("password")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.withUsername("other@example.com")
                .password("password")
                .roles("USER")
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void shouldRejectNonPositiveExpiration() {
        assertThatThrownBy(() -> new JwtService(SECRET, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiration-ms must be positive");
    }
}
